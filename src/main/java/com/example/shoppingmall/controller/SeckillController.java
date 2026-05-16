
package com.example.shoppingmall.controller;

import com.example.shoppingmall.dto.SeckillRequest;
import com.example.shoppingmall.service.SeckillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/seckill")
@RequiredArgsConstructor
public class SeckillController {

    private final SeckillService seckillService;

    @PostMapping("/buy")
    public ResponseEntity<String> seckill(@Valid @RequestBody SeckillRequest request) {
        log.info("秒杀请求，productId: {}, userId: {}", request.getProductId(), request.getUserId());
        String result = seckillService.seckill(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/warmup/{productId}")
    public ResponseEntity<String> warmupStock(Long productId) {
        seckillService.warmupSeckillStock(productId);
        return ResponseEntity.ok("库存预热完成");
    }
}
