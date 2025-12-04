package com.mrdinner.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_modification_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderModificationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "modified_at", nullable = false, updatable = false)
    private LocalDateTime modifiedAt = LocalDateTime.now();

    @Column(name = "previous_order_items", columnDefinition = "TEXT")
    private String previousOrderItems;

    @Column(name = "new_order_items", columnDefinition = "TEXT")
    private String newOrderItems;

    @Column(name = "price_difference", nullable = false)
    private Integer priceDifference;
}

