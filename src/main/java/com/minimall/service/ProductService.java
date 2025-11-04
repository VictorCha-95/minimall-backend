package com.minimall.service;

import com.minimall.domain.product.Product;
import com.minimall.domain.product.ProductRepository;
import com.minimall.service.exception.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public Product register(Product product) {
        return productRepository.save(product);
    }

    @Transactional
    public void delete(Long productId) {
        productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("id", productId));
        productRepository.deleteById(productId);
    }

    @Transactional
    public void addStock(Long productId, int stockQuantity) {

    }


    public Product findById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("id", productId));
    }

    public List<Product> findByName(String name) {
        return productRepository.findByName(name);
    }

    public List<Product> findByPriceLessThan(int price) {
        return productRepository.findByPriceLessThan(price);
    }

    public List<Product> findByPriceGreaterThan(int price) {
        return productRepository.findByPriceGreaterThan(price);
    }

    public List<Product> findByStockQuantityLessThan(int stockQuantity) {
        return productRepository.findByStockQuantityLessThan(stockQuantity);
    }

    public List<Product> findByStockQuantityGreaterThan(int stockQuantity) {
        return productRepository.findByStockQuantityGreaterThan(stockQuantity);
    }
}

