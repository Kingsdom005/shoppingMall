
package com.example.shoppingmall.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SeckillRequest {

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @Min(value = 1, message = "购买数量至少为1")
    @Max(value = 5, message = "单次购买数量不能超过5")
    private Integer quantity = 1;
}
