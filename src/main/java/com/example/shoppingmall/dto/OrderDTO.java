
package com.example.shoppingmall.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderDTO {

    private Long id;

    private Long userId;

    private Long productId;

    private Integer quantity;

    private BigDecimal price;

    private BigDecimal seckillPrice;

    private String orderNo;

    private Integer status;

    private LocalDateTime createdAt;
}
