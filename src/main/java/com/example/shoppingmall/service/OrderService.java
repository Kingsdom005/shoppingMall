
package com.example.shoppingmall.service;

import com.example.shoppingmall.dto.OrderDTO;
import com.example.shoppingmall.entity.Order;
import com.example.shoppingmall.entity.Product;
import com.example.shoppingmall.mapper.OrderMapper;
import com.example.shoppingmall.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;
    private final RedisService redisService;

    private static final String SECKILL_STOCK_KEY = "seckill:stock:";
    private static final String SECKILL_USER_KEY = "seckill:user:";

    public List<OrderDTO> listOrdersByUser(Long userId) {
        List<Order> orders = orderMapper.selectList(null);
        return orders.stream()
                .filter(order -> order.getUserId().equals(userId))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public OrderDTO getOrderById(Long id) {
        Order order = orderMapper.selectById(id);
        return order != null ? convertToDTO(order) : null;
    }

    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setUserId(order.getUserId());
        dto.setProductId(order.getProductId());
        dto.setQuantity(order.getQuantity());
        dto.setPrice(order.getPrice());
        dto.setSeckillPrice(order.getSeckillPrice());
        dto.setOrderNo(order.getOrderNo());
        dto.setStatus(order.getStatus());
        dto.setStatusText(getStatusText(order.getStatus()));
        dto.setCreatedAt(order.getCreatedAt());
        return dto;
    }

    private String getStatusText(Integer status) {
        switch (status) {
            case Order.STATUS_PENDING_PAYMENT:
                return "待支付";
            case Order.STATUS_PAID:
                return "已支付";
            case Order.STATUS_SHIPPED:
                return "已发货";
            case Order.STATUS_COMPLETED:
                return "已完成";
            case Order.STATUS_CANCELLED:
                return "已取消";
            default:
                return "未知";
        }
    }

    @Transactional
    public boolean payOrder(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            log.warn("订单不存在，orderId: {}", orderId);
            return false;
        }
        if (!order.getUserId().equals(userId)) {
            log.warn("订单不属于当前用户，orderId: {}, userId: {}", orderId, userId);
            return false;
        }
        if (order.getStatus() != Order.STATUS_PENDING_PAYMENT) {
            log.warn("订单状态不允许支付，orderId: {}, status: {}", orderId, order.getStatus());
            return false;
        }

        order.setStatus(Order.STATUS_PAID);
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);

        log.info("订单支付成功，orderId: {}, orderNo: {}", orderId, order.getOrderNo());
        return true;
    }

    @Transactional
    public boolean shipOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            log.warn("订单不存在，orderId: {}", orderId);
            return false;
        }
        if (order.getStatus() != Order.STATUS_PAID) {
            log.warn("订单状态不允许发货，orderId: {}, status: {}", orderId, order.getStatus());
            return false;
        }

        order.setStatus(Order.STATUS_SHIPPED);
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);

        log.info("订单发货成功，orderId: {}, orderNo: {}", orderId, order.getOrderNo());
        return true;
    }

    @Transactional
    public boolean confirmOrder(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            log.warn("订单不存在，orderId: {}", orderId);
            return false;
        }
        if (!order.getUserId().equals(userId)) {
            log.warn("订单不属于当前用户，orderId: {}, userId: {}", orderId, userId);
            return false;
        }
        if (order.getStatus() != Order.STATUS_SHIPPED) {
            log.warn("订单状态不允许确认收货，orderId: {}, status: {}", orderId, order.getStatus());
            return false;
        }

        order.setStatus(Order.STATUS_COMPLETED);
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);

        log.info("订单确认收货成功，orderId: {}, orderNo: {}", orderId, order.getOrderNo());
        return true;
    }

    @Transactional
    public boolean cancelOrder(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            log.warn("订单不存在，orderId: {}", orderId);
            return false;
        }
        if (!order.getUserId().equals(userId)) {
            log.warn("订单不属于当前用户，orderId: {}, userId: {}", orderId, userId);
            return false;
        }
        if (order.getStatus() != Order.STATUS_PENDING_PAYMENT) {
            log.warn("订单状态不允许取消，orderId: {}, status: {}", orderId, order.getStatus());
            return false;
        }

        Long productId = order.getProductId();
        Integer quantity = order.getQuantity();

        order.setStatus(Order.STATUS_CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);

        Product product = productMapper.selectById(productId);
        if (product != null) {
            productMapper.increaseSeckillStock(productId, quantity);

            String stockKey = SECKILL_STOCK_KEY + productId;
            redisService.increment(stockKey, quantity);

            String userKey = SECKILL_USER_KEY + productId + ":" + userId;
            redisService.delete(userKey);

            log.info("订单取消成功，恢复库存，orderId: {}, productId: {}, quantity: {}", orderId, productId, quantity);
        }

        log.info("订单取消成功，orderId: {}, orderNo: {}", orderId, order.getOrderNo());
        return true;
    }
}
