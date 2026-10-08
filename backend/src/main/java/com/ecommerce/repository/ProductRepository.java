package com.ecommerce.repository;

import com.ecommerce.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByUserId(Long userId);

    List<Product> findByCategory(String category);
}
