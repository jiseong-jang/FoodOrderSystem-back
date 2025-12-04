package com.mrdinner.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@PrimaryKeyJoinColumn(name = "user_id")
public class Customer extends User {
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "card_number", nullable = false, length = 19)
    private String cardNumber;

    @Column(name = "card_expiry", nullable = false, length = 5)
    private String cardExpiry;

    @Column(name = "card_cvc", nullable = false, length = 3)
    private String cardCVC;

    @Column(name = "card_holder_name", nullable = false)
    private String cardHolderName;

    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Cart cart;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> orders = new ArrayList<>();

    @Column(name = "is_regular_customer", nullable = false)
    private Boolean isRegularCustomer = false;
}

