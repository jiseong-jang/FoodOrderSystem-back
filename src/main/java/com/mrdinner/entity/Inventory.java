package com.mrdinner.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {
    @Id
    @Column(name = "item_name", unique = true, nullable = false)
    private String itemName;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 0;

    @Column(name = "last_restocked")
    private LocalDateTime lastRestocked;
}

