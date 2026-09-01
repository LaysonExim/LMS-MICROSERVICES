// Purpose: Unit tests for Customer Service
// File: customer-service/src/test/java/com/nextgenloan/customer/service/CustomerServiceTest.java
// Dependencies: Mockito, JUnit

package com.nextgenloan.customer.service;

import com.nextgenloan.customer.dto.CustomerRequestDto;
import com.nextgenloan.customer.dto.CustomerResponseDto;
import com.nextgenloan.customer.entity.Customer;
import com.nextgenloan.customer.exception.CustomerAlreadyExistsException;
import com.nextgenloan.customer.exception.CustomerNotFoundException;
import com.nextgenloan.customer.mapper.CustomerMapper;
import com.nextgenloan.customer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerService customerService;

    private CustomerRequestDto requestDto;
    private Customer customer;
    private CustomerResponseDto responseDto;

    @BeforeEach
    void setUp() {
        requestDto = CustomerRequestDto.builder()
                .customerNumber("CUST-UNIT-001")
                .firstName("Unit")
                .lastName("Test")
                .email("unit@test.com")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .build();

        customer = Customer.builder()
                .id(1L)
                .customerNumber("CUST-UNIT-001")
                .firstName("Unit")
                .lastName("Test")
                .email("unit@test.com")
                .status("PENDING_VERIFICATION")
                .build();

        responseDto = CustomerResponseDto.builder()
                .customerNumber("CUST-UNIT-001")
                .firstName("Unit")
                .lastName("Test")
                .email("unit@test.com")
                .status("PENDING_VERIFICATION")
                .build();
    }

    @Test
    void shouldRegisterCustomer() {
        // Given
        when(customerRepository.existsByEmail(anyString())).thenReturn(false);
        when(customerRepository.existsByCustomerNumber(anyString())).thenReturn(false);
        when(customerMapper.toEntityFromRequest(any())).thenReturn(customer);
        when(customerRepository.save(any())).thenReturn(customer);
        when(customerMapper.toResponseDto(any())).thenReturn(responseDto);

        // When
        CustomerResponseDto result = customerService.registerCustomer(requestDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCustomerNumber()).isEqualTo("CUST-UNIT-001");
        verify(customerRepository).save(any());
    }

    @Test
    void shouldThrowExceptionWhenEmailExists() {
        // Given
        when(customerRepository.existsByEmail(anyString())).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> customerService.registerCustomer(requestDto))
                .isInstanceOf(CustomerAlreadyExistsException.class)
                .hasMessageContaining("already exists");

        verify(customerRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenCustomerNotFound() {
        // Given
        when(customerRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> customerService.getCustomer(999L))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("Customer not found");
    }

    @Test
    void shouldGetCustomerByNumber() {
        // Given
        when(customerRepository.findByCustomerNumber(anyString())).thenReturn(Optional.of(customer));
        when(customerMapper.toResponseDto(any())).thenReturn(responseDto);

        // When
        CustomerResponseDto result = customerService.getCustomerByNumber("CUST-UNIT-001");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCustomerNumber()).isEqualTo("CUST-UNIT-001");
    }
}