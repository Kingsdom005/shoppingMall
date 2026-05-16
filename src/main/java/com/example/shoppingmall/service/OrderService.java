
package com.example.shoppingmall.service;

import com.example.shoppingmall.dto.OrderDTO;
import com.example.shoppingmall.entity.Order;
import com.example.shoppingmall.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;

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
        dto.setCreatedAt(order.getCreatedAt());
        return dto;
    }
}
