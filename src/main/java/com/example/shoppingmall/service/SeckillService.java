
package com.example.shoppingmall.service;

import com.example.shoppingmall.dto.SeckillRequest;
import com.example.shoppingmall.entity.Order;
import com.example.shoppingmall.entity.Product;
import com.example.shoppingmall.mapper.OrderMapper;
import com.example.shoppingmall.mapper.ProductMapper;
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

    @Value("${seckill.max-purchase:5}")
    private int maxPurchase;

    private static final String SECKILL_STOCK_KEY = "seckill:stock:";
    private static final String SECKILL_USER_KEY = "seckill:user:";
    private static final String SECKILL_LOCK_KEY = "seckill:lock:";

    public String seckill(SeckillRequest request) {
        Long productId = request.getProductId();
        Long userId = request.getUserId();
        Integer quantity = request.getQuantity();

        if (quantity > maxPurchase) {
            return "单次购买数量不能超过" + maxPurchase;
        }

        String stockKey = SECKILL_STOCK_KEY + productId;
        String userKey = SECKILL_USER_KEY + productId + ":" + userId;

        if (Boolean.TRUE.equals(redisService.hasKey(userKey))) {
            return "您已参与过本次秒杀";
        }

        Long stock = redisService.decrement(stockKey, quantity);
        if (stock == null || stock < 0) {
            if (stock != null && stock < 0) {
                redisService.increment(stockKey, quantity);
            }
            return "秒杀已结束或库存不足";
        }

        redisService.set(userKey, userId, 24, TimeUnit.HOURS);

        rabbitTemplate.convertAndSend("seckill.exchange", "seckill.order", request);

        return "秒杀成功，正在处理订单...";
    }

    @Transactional
    public void processSeckillOrder(SeckillRequest request) {
        Long productId = request.getProductId();
        Long userId = request.getUserId();
        Integer quantity = request.getQuantity();

        String lockKey = SECKILL_LOCK_KEY + productId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (!lock.tryLock(3, 10, TimeUnit.SECONDS)) {
                log.warn("获取锁失败，productId: {}", productId);
                return;
            }

            Product product = productMapper.selectById(productId);
            if (product == null || product.getSeckillStatus() != 1) {
                log.warn("商品不存在或未开启秒杀，productId: {}", productId);
                return;
            }

            if (product.getSeckillStock() < quantity) {
                log.warn("库存不足，productId: {}, stock: {}", productId, product.getSeckillStock());
                return;
            }

            int updated = productMapper.decreaseSeckillStock(productId, quantity);
            if (updated == 0) {
                log.warn("扣减库存失败，productId: {}", productId);
                return;
            }

            Order order = new Order();
            order.setUserId(userId);
            order.setProductId(productId);
            order.setQuantity(quantity);
            order.setPrice(product.getPrice());
            order.setSeckillPrice(product.getSeckillPrice());
            order.setOrderNo(generateOrderNo());
            order.setStatus(1);
            order.setCreatedAt(LocalDateTime.now());
            order.setUpdatedAt(LocalDateTime.now());

            orderMapper.insert(order);

            log.info("订单创建成功，orderNo: {}", order.getOrderNo());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("处理秒杀订单中断", e);
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
