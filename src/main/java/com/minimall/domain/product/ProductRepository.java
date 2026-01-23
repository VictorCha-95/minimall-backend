package com.minimall.domain.product;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByName(String name);

    List<Product> findByPriceLessThan(int price);

    List<Product> findByPriceGreaterThan(int price);

    List<Product> findByStockQuantityLessThan(int stockQuantity);

    List<Product> findByStockQuantityGreaterThan(int stockQuantity);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);

    Slice<Product> findByNameContainingIgnoreCaseOrderByCreatedAtDesc(String name, Pageable pageable);

    Slice<Product> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
