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
@Table(name = "cart_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu selectedMenu;

    @Enumerated(EnumType.STRING)
    @Column(name = "selected_style", nullable = false)
    private StyleType selectedStyle;

    @Column(name = "customized_quantities", columnDefinition = "TEXT")
    @Convert(converter = MapConverter.class)
    private Map<String, Integer> customizedQuantities = new HashMap<>();

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;

    @Column(name = "sub_total", nullable = false)
    private Integer subTotal = 0;
}

