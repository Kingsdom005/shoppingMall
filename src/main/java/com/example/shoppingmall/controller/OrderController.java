
package com.example.shoppingmall.controller;

import com.example.shoppingmall.dto.OrderDTO;
import com.example.shoppingmall.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderDTO>> listOrdersByUser(@PathVariable Long userId) {
        List<OrderDTO> orders = orderService.listOrdersByUser(userId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable Long id) {
        OrderDTO order = orderService.getOrderById(id);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{orderId}/pay")
    public ResponseEntity<Map<String, Object>> payOrder(@PathVariable Long orderId, @RequestParam Long userId) {
        log.info("支付请求，orderId: {}, userId: {}", orderId, userId);
        
        Map<String, Object> response = new HashMap<>();
        
        boolean success = orderService.payOrder(orderId, userId);
        if (success) {
            response.put("success", true);
            response.put("message", "支付成功");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "支付失败");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/{orderId}/ship")
    public ResponseEntity<Map<String, Object>> shipOrder(@PathVariable Long orderId) {
        log.info("发货请求，orderId: {}", orderId);
        
        Map<String, Object> response = new HashMap<>();
        
        boolean success = orderService.shipOrder(orderId);
        if (success) {
            response.put("success", true);
            response.put("message", "发货成功");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "发货失败");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<Map<String, Object>> confirmOrder(@PathVariable Long orderId, @RequestParam Long userId) {
        log.info("确认收货请求，orderId: {}, userId: {}", orderId, userId);
        
        Map<String, Object> response = new HashMap<>();
        
        boolean success = orderService.confirmOrder(orderId, userId);
        if (success) {
            response.put("success", true);
            response.put("message", "确认收货成功");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "确认收货失败");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelOrder(@PathVariable Long orderId, @RequestParam Long userId) {
        log.info("取消订单请求，orderId: {}, userId: {}", orderId, userId);
        
        Map<String, Object> response = new HashMap<>();
        
        boolean success = orderService.cancelOrder(orderId, userId);
        if (success) {
            response.put("success", true);
            response.put("message", "取消订单成功");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "取消订单失败，订单状态不允许取消");
            return ResponseEntity.badRequest().body(response);
        }
    }
}
