package com.phuc.accounts.service;

import com.phuc.accounts.dto.CustomerDetailsDto;

public interface CustomersService {
    CustomerDetailsDto fetchCustomerDetails(String mobileNumber);
}
