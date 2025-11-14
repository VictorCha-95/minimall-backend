package com.minimall.service.product;

import com.minimall.domain.product.Product;
import com.minimall.domain.product.ProductRepository;
import com.minimall.service.exception.ProductNotFoundException;
import com.minimall.service.product.dto.ProductRegisterCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    public Product register(ProductRegisterCommand command) {
        Product product = new Product(command.name(), command.price(), command.stockQuantity());
        return productRepository.save(product);
    }

    public void addStock(Long id, int requestedQuantity) {
        Product product = findById(id);
        product.addStock(requestedQuantity);
    }

    public void reduceStock(Long id, int requestedQuantity) {
        Product product = findById(id);
        product.reduceStock(requestedQuantity);
    }

    public void clearStock(Long id) {
        Product product = findById(id);
        product.clearStock();
    }

    public void changeName(Long id, String name) {
        Product product = findById(id);
        product.changeName(name);
    }

    public void changePrice(Long id, int price) {
        Product product = findById(id);
        product.changePrice(price);
    }

    public void delete(Long id) {
        productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("id", id));
        productRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("id", id));
    }

    @Transactional(readOnly = true)
    public List<Product> findByName(String name) {
        return productRepository.findByName(name);
    }

    @Transactional(readOnly = true)
    public List<Product> findByPriceLessThan(int price) {
        return productRepository.findByPriceLessThan(price);
    }

    @Transactional(readOnly = true)
    public List<Product> findByPriceGreaterThan(int price) {
        return productRepository.findByPriceGreaterThan(price);
    }

    @Transactional(readOnly = true)
    public List<Product> findByStockQuantityLessThan(int stockQuantity) {
        return productRepository.findByStockQuantityLessThan(stockQuantity);
    }

    @Transactional(readOnly = true)
    public List<Product> findByStockQuantityGreaterThan(int stockQuantity) {
        return productRepository.findByStockQuantityGreaterThan(stockQuantity);
    }
}

