package com.minimall.service.product;

import com.minimall.api.order.dto.request.OrderCreateRequest;
import com.minimall.api.order.dto.request.OrderItemCreateRequest;
import com.minimall.api.order.dto.response.OrderItemResponse;
import com.minimall.domain.embeddable.Address;
import com.minimall.domain.member.Member;
import com.minimall.domain.product.Product;
import com.minimall.domain.product.ProductRepository;
import com.minimall.service.order.dto.OrderCreateCommand;
import com.minimall.service.order.dto.OrderItemCreateCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    ProductRepository productRepository;

    @Mock
    ProductService productService;

    private Product book;
    private Product keyboard;

    private final Long MEMBER_ID = 100L;
    private final Long PRODUCT1_ID = 1L;
    private final Long PRODUCT2_ID = 2L;



    @BeforeEach
    void setUp() {
        //== Product Entity ==//
        book = new Product("도서", 20000, 50);
        keyboard = new Product("키보드", 100000, 20);
    }


}
