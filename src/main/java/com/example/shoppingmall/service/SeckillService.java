
package com.example.shoppingmall.service;

import com.example.shoppingmall.dto.SeckillRequest;
import com.example.shoppingmall.entity.Order;
import com.example.shoppingmall.entity.Product;
import com.example.shoppingmall.mapper.OrderMapper;
import com.example.shoppingmall.mapper.ProductMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillService {

    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;
    private final RedisService redisService;
    private final RedissonClient redissonClient;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${seckill.max-purchase:5}")
    private int maxPurchase;

    private static final String SECKILL_STOCK_KEY = "seckill:stock:";
    private static final String SECKILL_USER_KEY = "seckill:user:";
    private static final String SECKILL_LOCK_KEY = "seckill:lock:";

    public String seckill(SeckillRequest request) {
        Long productId = request.getProductId();
        Long userId = request.getUserId();
        Integer quantity = request.getQuantity();

        log.info("开始秒杀，productId: {}, userId: {}, quantity: {}", productId, userId, quantity);

        if (quantity > maxPurchase) {
            log.warn("单次购买数量超限，maxPurchase: {}, quantity: {}", maxPurchase, quantity);
            return "单次购买数量不能超过" + maxPurchase;
        }

        String stockKey = SECKILL_STOCK_KEY + productId;
        String userKey = SECKILL_USER_KEY + productId + ":" + userId;

        if (Boolean.TRUE.equals(redisService.hasKey(userKey))) {
            log.warn("用户已参与过秒杀，userId: {}, productId: {}", userId, productId);
            return "您已参与过本次秒杀";
        }

        Product product = productMapper.selectById(productId);
        if (product == null) {
            log.warn("商品不存在，productId: {}", productId);
            return "商品不存在";
        }
        if (product.getSeckillStatus() != 1) {
            log.warn("商品未开启秒杀，productId: {}, status: {}", productId, product.getSeckillStatus());
            return "商品未开启秒杀";
        }

        LocalDateTime now = LocalDateTime.now();
        if (product.getSeckillStartTime() != null && now.isBefore(product.getSeckillStartTime())) {
            log.warn("秒杀尚未开始，productId: {}, startTime: {}", productId, product.getSeckillStartTime());
            return "秒杀尚未开始";
        }
        if (product.getSeckillEndTime() != null && now.isAfter(product.getSeckillEndTime())) {
            log.warn("秒杀已结束，productId: {}, endTime: {}", productId, product.getSeckillEndTime());
            return "秒杀已结束";
        }

        Object stockObj = redisService.get(stockKey);
        Long stock = convertToLong(stockObj);
        if (stock == null) {
            log.info("Redis库存不存在，从数据库加载，seckillStock: {}", product.getSeckillStock());
            if (product.getSeckillStock() < quantity) {
                log.warn("数据库库存不足，seckillStock: {}, quantity: {}", product.getSeckillStock(), quantity);
                return "秒杀库存不足";
            }
            redisService.set(stockKey, product.getSeckillStock());
            log.info("初始化Redis库存，stockKey: {}, value: {}", stockKey, product.getSeckillStock());
            stock = stockObj != null ? convertToLong(stockObj) : null;
        }

        if (stock == null) {
            stock = product.getSeckillStock().longValue();
        }

        stock = redisService.decrement(stockKey, quantity);
        log.info("Redis扣减库存结果，stockKey: {}, stock: {}, quantity: {}", stockKey, stock, quantity);

        if (stock == null || stock < 0) {
            if (stock != null) {
                redisService.increment(stockKey, quantity);
            }
            log.warn("Redis库存不足，stock: {}", stock);
            return "秒杀库存不足";
        }

        boolean orderSuccess = processSeckillOrderWithResult(request);

        if (orderSuccess) {
            redisService.set(userKey, userId, 24, TimeUnit.HOURS);
            log.info("记录用户参与，userKey: {}", userKey);
            log.info("订单处理完成，productId: {}, userId: {}", productId, userId);
            return "秒杀成功，订单已创建";
        } else {
            redisService.increment(stockKey, quantity);
            log.warn("订单处理失败，回滚库存，productId: {}, userId: {}", productId, userId);
            return "秒杀失败，请重试";
        }
    }

    private Long convertToLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.parseLong(value.toString());
    }

    @Transactional
    public boolean processSeckillOrderWithResult(SeckillRequest request) {
        Long productId = request.getProductId();
        Long userId = request.getUserId();
        Integer quantity = request.getQuantity();

        String lockKey = SECKILL_LOCK_KEY + productId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (!lock.tryLock(3, 10, TimeUnit.SECONDS)) {
                log.warn("获取锁失败，productId: {}", productId);
                return false;
            }

            Product product = productMapper.selectById(productId);
            if (product == null || product.getSeckillStatus() != 1) {
                log.warn("商品不存在或未开启秒杀，productId: {}", productId);
                return false;
            }

            if (product.getSeckillStock() < quantity) {
                log.warn("库存不足，productId: {}, stock: {}", productId, product.getSeckillStock());
                return false;
            }

            int updated = productMapper.decreaseSeckillStock(productId, quantity);
            if (updated == 0) {
                log.warn("扣减库存失败，productId: {}", productId);
                return false;
            }

            Order order = new Order();
            order.setUserId(userId);
            order.setProductId(productId);
            order.setQuantity(quantity);
            order.setPrice(product.getPrice());
            order.setSeckillPrice(product.getSeckillPrice());
            order.setOrderNo(generateOrderNo());
            order.setStatus(Order.STATUS_PENDING_PAYMENT);
            order.setCreatedAt(LocalDateTime.now());
            order.setUpdatedAt(LocalDateTime.now());

            orderMapper.insert(order);

            log.info("订单创建成功，orderNo: {}, status: 待支付", order.getOrderNo());
            return true;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("处理秒杀订单中断", e);
            return false;
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    public void warmupSeckillStock(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product != null && product.getSeckillStatus() == 1) {
            String stockKey = SECKILL_STOCK_KEY + productId;
            redisService.set(stockKey, product.getSeckillStock());
            log.info("库存预热完成，productId: {}, stock: {}", productId, product.getSeckillStock());
        }
    }

    private String generateOrderNo() {
        return "SK" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
