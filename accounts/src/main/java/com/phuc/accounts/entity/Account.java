package com.phuc.accounts.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Setter @Getter @ToString
@NoArgsConstructor
@AllArgsConstructor
public class Account extends BaseEntity {
    @Column(name="account_number")
    @Id
    private Long accountNumber;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    private String accountType;
    private String branchAddress;
}
