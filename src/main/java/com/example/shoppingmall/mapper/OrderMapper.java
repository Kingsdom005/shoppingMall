
package com.example.shoppingmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.shoppingmall.entity.Order;
import org.apache.ibatis.annotations.Param;

public interface OrderMapper extends BaseMapper<Order> {

    int countSeckillOrdersByUserAndProduct(@Param("userId") Long userId, @Param("productId") Long productId);
}
