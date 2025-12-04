package com.mrdinner.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "staff")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
@PrimaryKeyJoinColumn(name = "user_id")
public abstract class Staff extends User {
    @Column(name = "employee_id", unique = true, nullable = false)
    private String employeeId;

    @Column(name = "is_busy", nullable = false)
    private Boolean isBusy = false;
}

