package com.mrdinner.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerProfileDto {
    private String id;
    private String name;
    private String address;
    private String cardNumber;
    private String cardExpiry;
    private String cardCVC;
    private String cardHolderName;
    private Boolean isRegularCustomer;
}

