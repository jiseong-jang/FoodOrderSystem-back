package com.mrdinner.repository;

import com.mrdinner.entity.Cart;
import com.mrdinner.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByCustomer(Customer customer);
    Optional<Cart> findByCustomer_Id(String customerId);
}

