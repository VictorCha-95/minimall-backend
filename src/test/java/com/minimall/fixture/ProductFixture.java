package com.minimall.fixture;

import com.minimall.api.product.dto.request.ProductRegisterRequest;
import com.minimall.domain.product.Product;
import com.minimall.domain.product.ProductRepository;
import com.minimall.service.product.dto.ProductRegisterCommand;

public final class ProductFixture {

    public static final String DEFAULT_NAME = "테스트상품";
    public static final int DEFAULT_PRICE = 10_000;
    public static final int DEFAULT_STOCK = 50;

    private ProductFixture() {
    }

    public static Product createProduct() {
        return createProduct(DEFAULT_NAME, DEFAULT_PRICE, DEFAULT_STOCK);
    }

    public static Product createProduct(String name, int price, int stock) {
        return new Product(name, price, stock);
    }

    public static Product createProductSaved(ProductRepository repository) {
        return repository.save(createProduct());
    }

    public static Product createProductSaved(ProductRepository repository, String name, int price, int stock) {
        return repository.save(createProduct(name, price, stock));
    }

    public static ProductRegisterRequest createProductRegisterRequest(String name, int price, int stock) {
        return new ProductRegisterRequest(name, price, stock);
    }

    public static ProductRegisterCommand createProductRegisterCommand(String name, int price, int stock) {
        return new ProductRegisterCommand(name, price, stock);
    }
}
