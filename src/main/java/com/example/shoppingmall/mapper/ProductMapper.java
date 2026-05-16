
package com.example.shoppingmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.shoppingmall.entity.Product;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface ProductMapper extends BaseMapper<Product> {

    @Update("UPDATE product SET seckill_stock = seckill_stock - #{quantity} WHERE id = #{productId} AND seckill_stock >= #{quantity}")
    int decreaseSeckillStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    @Update("UPDATE product SET stock = stock - #{quantity} WHERE id = #{productId} AND stock >= #{quantity}")
    int decreaseStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    @Update("UPDATE product SET seckill_stock = seckill_stock + #{quantity} WHERE id = #{productId}")
    int increaseSeckillStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
}
