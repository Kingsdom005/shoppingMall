
package com.example.shoppingmall.consumer;

import com.example.shoppingmall.dto.SeckillRequest;
import com.example.shoppingmall.service.SeckillService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillConsumer {

    private final SeckillService seckillService;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @RabbitListener(queues = "seckill.queue")
    public void processSeckillOrder(Message message) {
        try {
            String body = new String(message.getBody());
            SeckillRequest request = objectMapper.readValue(body, SeckillRequest.class);
            
            log.info("收到秒杀订单消息，productId: {}, userId: {}", request.getProductId(), request.getUserId());
            
            seckillService.processSeckillOrder(request);
            
        } catch (JsonProcessingException e) {
            log.error("解析秒杀订单消息失败", e);
        } catch (Exception e) {
            log.error("处理秒杀订单失败", e);
        }
    }
}
