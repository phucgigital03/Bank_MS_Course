package com.phuc.accounts.service.impl;

import com.phuc.accounts.dto.AccountDto;
import com.phuc.accounts.dto.CardsDto;
import com.phuc.accounts.dto.CustomerDetailsDto;
import com.phuc.accounts.dto.LoansDto;
import com.phuc.accounts.entity.Account;
import com.phuc.accounts.entity.Customer;
import com.phuc.accounts.exception.ResourceNotFoundException;
import com.phuc.accounts.mapper.AccountMapper;
import com.phuc.accounts.mapper.CustomerMapper;
import com.phuc.accounts.repository.AccountRepository;
import com.phuc.accounts.repository.CustomerRepository;
import com.phuc.accounts.service.CustomersService;
import com.phuc.accounts.service.client.CardsFeignClient;
import com.phuc.accounts.service.client.LoansFeignClient;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
//@RequiredArgsConstructor
@AllArgsConstructor
public class CustomersServiceImpl implements CustomersService {
    private AccountRepository accountsRepository;
    private CustomerRepository customerRepository;
    private CardsFeignClient cardsFeignClient;
    private LoansFeignClient loansFeignClient;

    @Override
    public CustomerDetailsDto fetchCustomerDetails(String mobileNumber, String correlationId) {
        Customer customer = customerRepository.findByMobileNumber(mobileNumber)
                .orElseThrow(() -> new ResourceNotFoundException("customer","mobileNumber", mobileNumber));

        Account account = accountsRepository.findByCustomerId(customer.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("account", "customerId", customer.getCustomerId().toString()));

        CustomerDetailsDto customerDetailsDto = CustomerMapper.mapToCustomerDetailsDto(customer, new CustomerDetailsDto());
        customerDetailsDto.setAccountsDto(AccountMapper.mapToAccountDto(account, new AccountDto()));

        // Fetch cards and loans details using Feign clients
        ResponseEntity<LoansDto> loansResponseEntity = loansFeignClient.fetchLoanDetails(correlationId,mobileNumber);
        if(loansResponseEntity != null){
            customerDetailsDto.setLoansDto(loansResponseEntity.getBody());
        }

        ResponseEntity<CardsDto> cardsResponseEntity = cardsFeignClient.fetchCardDetails(correlationId,mobileNumber);
        if(cardsResponseEntity != null){
            customerDetailsDto.setCardsDto(cardsResponseEntity.getBody());
        }

        return customerDetailsDto;
    }
}
