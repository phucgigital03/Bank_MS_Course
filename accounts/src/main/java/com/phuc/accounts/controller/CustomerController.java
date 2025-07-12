package com.phuc.accounts.controller;

import com.phuc.accounts.dto.CustomerDetailsDto;
import com.phuc.accounts.service.CustomersService;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.apache.hc.core5.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping(path="/api", produces = {MediaType.APPLICATION_JSON_VALUE})
@RequiredArgsConstructor
public class CustomerController {

    private final CustomersService CustomersService;

    @GetMapping("/fetchCustomerDetails")
    public ResponseEntity<CustomerDetailsDto> fetchCustomerDetails(@RequestParam
                                                                   @Pattern(regexp="(^$|[0-9]{10})",message = "Mobile number must be 10 digits")
                                                                   String mobileNumber){
        CustomerDetailsDto customerDetailsDto = CustomersService.fetchCustomerDetails(mobileNumber);
        return ResponseEntity.status(HttpStatus.SC_OK).body(customerDetailsDto);
    }

}
