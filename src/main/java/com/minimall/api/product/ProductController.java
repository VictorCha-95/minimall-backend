package com.minimall.api.product;

import com.minimall.api.product.dto.request.ProductRegisterRequest;
import com.minimall.domain.product.Product;
import com.minimall.service.product.ProductService;
import com.minimall.service.product.dto.ProductRegisterCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping(value = "/products", produces = "application/json")
@Tag(name = "Product API", description = "상품 관련 API")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "상품 등록")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "등록 성공"),
            @ApiResponse(responseCode = "400", description = "요청 검증 오류"),
            @ApiResponse(responseCode = "422", description = "상품 등록 필드 규칙 오류")
    })
    @PostMapping
    public ResponseEntity<Void> registerProduct(@RequestBody ProductRegisterRequest request) {

        ProductRegisterCommand command = new ProductRegisterCommand(
                request.name(),
                request.price(),
                request.stockQuantity()
        );

        Product product = productService.register(command);

        return ResponseEntity
                .created(URI.create("/product/" + product.getId().toString()))
                .build();
    }

    @Operation(summary = "재고 증가")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "재고 증가 성공"),
            @ApiResponse(responseCode = "400", description = "요청 검증 오류"),
            @ApiResponse(responseCode = "422", description = "상품 찾을 수 없음 or 요청 수량 오류")
    })
    @PostMapping("/{id}/stock/add")
    public ResponseEntity<Void> addStock(@PathVariable Long id,
                                         @RequestParam int requestedQuantity) {

        productService.addStock(id, requestedQuantity);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "재고 감소")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "재고 감소 성공"),
            @ApiResponse(responseCode = "400", description = "요청 검증 오류"),
            @ApiResponse(responseCode = "422", description = "재고 부족 or 상품 찾을 수 없음 or 요청 수량 오류")
    })
    @PostMapping("/{id}/stock/reduce")
    public ResponseEntity<Void> reduceStock(@PathVariable Long id,
                                            @RequestParam int requestedQuantity) {

        productService.reduceStock(id, requestedQuantity);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "재고 초기화")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "재고 초기화 성공"),
            @ApiResponse(responseCode = "400", description = "요청 검증 오류"),
            @ApiResponse(responseCode = "422", description = "상품 찾을 수 없음")
    })
    @PostMapping("/{id}/stock/clear")
    public ResponseEntity<Void> clearStock(@PathVariable Long id) {

        productService.clearStock(id);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "상품명 변경")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상품명 변경 성공"),
            @ApiResponse(responseCode = "400", description = "요청 검증 오류"),
            @ApiResponse(responseCode = "422", description = "상품 찾을 수 없음, 상품명 규칙 오류")
    })
    @PatchMapping("/{id}/name")
    public ResponseEntity<Void> changeName(@PathVariable Long id,
                                           @RequestParam String name) {

        productService.changeName(id, name);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "상품 가격 변경")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상품 가격 변경 성공"),
            @ApiResponse(responseCode = "400", description = "요청 검증 오류"),
            @ApiResponse(responseCode = "422", description = "상품 찾을 수 없음, 상품 가격 규칙 오류")
    })
    @PatchMapping("/{id}/price")
    public ResponseEntity<Void> changePrice(@PathVariable Long id,
                                           @RequestParam int price) {

        productService.changePrice(id, price);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "상품 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상품 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "요청 검증 오류"),
            @ApiResponse(responseCode = "422", description = "상품 찾을 수 없음")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){

        productService.delete(id);

        return ResponseEntity.noContent().build();
    }

}
