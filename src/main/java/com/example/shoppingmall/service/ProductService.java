
package com.example.shoppingmall.service;

import com.example.shoppingmall.dto.ProductDTO;
import com.example.shoppingmall.entity.Product;
import com.example.shoppingmall.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;
    private final SeckillService seckillService;

    public List<ProductDTO> listProducts() {
        List<Product> products = productMapper.selectList(null);
        return products.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public ProductDTO getProductById(Long id) {
        Product product = productMapper.selectById(id);
        return product != null ? convertToDTO(product) : null;
    }

    public ProductDTO createProduct(ProductDTO dto) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setSeckillPrice(dto.getSeckillPrice());
        product.setSeckillStock(dto.getSeckillStock());
        product.setSeckillStartTime(dto.getSeckillStartTime());
        product.setSeckillEndTime(dto.getSeckillEndTime());
        product.setSeckillStatus(dto.getSeckillStatus());
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        productMapper.insert(product);
        
        if (product.getSeckillStatus() == 1) {
            seckillService.warmupSeckillStock(product.getId());
        }
        
        return convertToDTO(product);
    }

    public ProductDTO updateProduct(Long id, ProductDTO dto) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            return null;
        }

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setSeckillPrice(dto.getSeckillPrice());
        product.setSeckillStock(dto.getSeckillStock());
        product.setSeckillStartTime(dto.getSeckillStartTime());
        product.setSeckillEndTime(dto.getSeckillEndTime());
        product.setSeckillStatus(dto.getSeckillStatus());
        product.setUpdatedAt(LocalDateTime.now());

        productMapper.updateById(product);

        if (product.getSeckillStatus() == 1) {
            seckillService.warmupSeckillStock(product.getId());
        }

        return convertToDTO(product);
    }

    public boolean deleteProduct(Long id) {
        return productMapper.deleteById(id) > 0;
    }

    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());
        dto.setSeckillPrice(product.getSeckillPrice());
        dto.setSeckillStock(product.getSeckillStock());
        dto.setSeckillStartTime(product.getSeckillStartTime());
        dto.setSeckillEndTime(product.getSeckillEndTime());
        dto.setSeckillStatus(product.getSeckillStatus());
        dto.setCreatedAt(product.getCreatedAt());
        return dto;
    }
}
