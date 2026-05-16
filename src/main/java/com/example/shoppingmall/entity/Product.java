
package com.example.shoppingmall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("product")
public class Product {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String description;

    private BigDecimal price;

    private Integer stock;

    @TableField("seckill_price")
    private BigDecimal seckillPrice;

    @TableField("seckill_stock")
    private Integer seckillStock;

    @TableField("seckill_start_time")
    private LocalDateTime seckillStartTime;

    @TableField("seckill_end_time")
    private LocalDateTime seckillEndTime;

    @TableField("seckill_status")
    private Integer seckillStatus;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
