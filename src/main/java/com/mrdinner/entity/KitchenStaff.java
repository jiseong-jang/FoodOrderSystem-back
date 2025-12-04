package com.mrdinner.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "kitchen_staff")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@PrimaryKeyJoinColumn(name = "staff_id")
public class KitchenStaff extends Staff {
    @OneToMany(mappedBy = "kitchenStaff", fetch = FetchType.LAZY)
    private List<Order> orders = new ArrayList<>();
}

