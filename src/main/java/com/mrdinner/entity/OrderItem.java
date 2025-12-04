package com.mrdinner.entity;

import com.mrdinner.util.MapConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @Enumerated(EnumType.STRING)
    @Column(name = "style_type", nullable = false)
    private StyleType styleType;

    @Column(name = "customized_quantities", columnDefinition = "TEXT")
    @Convert(converter = MapConverter.class)
    private Map<String, Integer> customizedQuantities = new HashMap<>();

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "sub_total", nullable = false)
    private Integer subTotal;
}

