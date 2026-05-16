
package com.example.shoppingmall.consumer;

import com.example.shoppingmall.dto.SeckillRequest;
import com.example.shoppingmall.service.SeckillService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillConsumer {

    private final SeckillService seckillService;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "seckill.queue")
    public void processSeckillOrder(Message message) {
        log.info("收到秒杀订单消息，消息体长度: {}", message.getBody().length);
        
        try {
            String body = new String(message.getBody(), "UTF-8");
            log.info("消息体内容: {}", body);
            
            SeckillRequest request = objectMapper.readValue(body, SeckillRequest.class);
            log.info("反序列化成功，productId: {}, userId: {}", request.getProductId(), request.getUserId());
            
            seckillService.processSeckillOrderWithResult(request);
            
            log.info("订单处理成功");
            
        } catch (Exception e) {
            log.error("处理秒杀订单失败", e);
            throw new RuntimeException("处理秒杀订单失败", e);
        }
    }
}
