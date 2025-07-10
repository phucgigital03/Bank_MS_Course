package com.phuc.accounts.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(
        name = "Accounts",
        description = "Schema to hold Account information"
)
public class AccountDto {
    @NotEmpty(message = "Account number cannot be empty")
    @Pattern(regexp = "^[0-9]{10}$", message = "Account number must be exactly 10 digits")
    @Schema(
            description = "Account Number of Phuc Bank account", example = "3454433243"
    )
    private Long accountNumber;

    @NotEmpty(message = "Account type name cannot be empty")
    @Schema(
            description = "Account type of Phuc Bank account", example = "Savings"
    )
    private String accountType;

    @NotEmpty(message = "Branch name cannot be empty")
    @Schema(
            description = "Phuc Bank branch address", example = "123 NewYork"
    )
    private String branchAddress;
}
