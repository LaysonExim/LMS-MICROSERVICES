// Purpose: Updated Customer Service with new DTOs
// File: customer-service/src/main/java/com/nextgenloan/customer/service/CustomerService.java
// Dependencies: CustomerRepository, CustomerMapper

package com.nextgenloan.customer.service;

import com.nextgenloan.customer.dto.*;
import com.nextgenloan.customer.entity.Address;
import com.nextgenloan.customer.entity.Customer;
import com.nextgenloan.customer.exception.CustomerAlreadyExistsException;
import com.nextgenloan.customer.exception.CustomerNotFoundException;
import com.nextgenloan.customer.mapper.CustomerMapper;
import com.nextgenloan.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nextgenloan.customer.mapper.AddressMapper;

import java.util.List;
import java.util.UUID;

/**
 * Customer Service
 *
 * WHY: The service layer contains ALL business logic.
 * It validates, transforms, and orchestrates data operations.
 *
 * UPDATED: Now using different DTOs for different use cases:
 * - CustomerRequestDto for incoming data
 * - CustomerResponseDto for outgoing data
 * - CustomerSummaryDto for lists
 * - CustomerInternalDto for service-to-service communication
 *
 * REAL BANK EXAMPLE: This separation allows us to:
 * 1. Validate data differently for different use cases
 * 2. Control what data is exposed at each boundary
 * 3. Evolve the API independently of the database
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final AddressMapper addressMapper;

    /**
     * Register a new customer
     *
     * WHY: Creates a new customer from the request DTO
     *
     * PROCESS:
     * 1. Validate email uniqueness
     * 2. Validate customer number uniqueness
     * 3. Map Request DTO to Entity
     * 4. Set default status and audit fields
     * 5. Save to database
     * 6. Return Response DTO
     *
     * SECURITY: We set status to 'PENDING_VERIFICATION' for new customers.
     * In a real bank, this would trigger KYC checks.
     */
    @Transactional
    public CustomerResponseDto registerCustomer(CustomerRequestDto requestDto) {
        log.info("Registering new customer with email: {}", requestDto.getEmail());

        // Validate email uniqueness
        if (customerRepository.existsByEmail(requestDto.getEmail())) {
            throw new CustomerAlreadyExistsException(
                    "Customer with email " + requestDto.getEmail() + " already exists"
            );
        }

        // Generate customer number if not provided
        String customerNumber = requestDto.getCustomerNumber();
        if (customerNumber == null || customerNumber.isBlank()) {
            customerNumber = generateCustomerNumber();
            requestDto.setCustomerNumber(customerNumber);
        }

        // Validate customer number uniqueness
        if (customerRepository.existsByCustomerNumber(customerNumber)) {
            throw new CustomerAlreadyExistsException(
                    "Customer with number " + customerNumber + " already exists"
            );
        }

        // Map DTO to entity
        Customer customer = customerMapper.toEntityFromRequest(requestDto);

        //
        // EXPLANATION: Setting default status
        // New customers start with 'PENDING_VERIFICATION' status.
        // In a real bank, this would mean:
        // 1. KYC checks need to be completed
        // 2. Identity verification is pending
        // 3. Customer can't use all banking features yet
        //
        customer.setStatus("PENDING_VERIFICATION");

        //
        // EXPLANATION: Setting audit fields
        // In a real bank, we'd get the authenticated user from the
        // security context. For now, we use a system user.
        //
        customer.setCreatedBy("SYSTEM");
        customer.setUpdatedBy("SYSTEM");

        // Save the customer
        Customer savedCustomer = customerRepository.save(customer);

        log.info("Customer registered successfully with ID: {}", savedCustomer.getId());

        // Return Response DTO
        return customerMapper.toResponseDto(savedCustomer);
    }

    /**
     * Get a customer by ID (internal use only)
     */
    public CustomerResponseDto getCustomer(Long id) {
        log.debug("Fetching customer with ID: {}", id);

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + id));

        return customerMapper.toResponseDto(customer);
    }

    /**
     * Get a customer by customer number (external clients)
     */
    public CustomerResponseDto getCustomerByNumber(String customerNumber) {
        log.debug("Fetching customer with number: {}", customerNumber);

        Customer customer = customerRepository.findByCustomerNumber(customerNumber)
                .orElseThrow(() -> new CustomerNotFoundException(
                        "Customer not found with number: " + customerNumber
                ));

        return customerMapper.toResponseDto(customer);
    }

    /**
     * Get customer details for internal service communication
     *
     * WHY: This endpoint is used by other services (Loan, Limit, etc.)
     * They need more data than external clients.
     *
     * SECURITY: This endpoint would be secured with service-to-service
     * authentication (e.g., client credentials with limited scope).
     * In a real bank, only authorized internal services can access this.
     */
    public CustomerInternalDto getCustomerInternal(String customerNumber) {
        log.debug("Fetching customer internally with number: {}", customerNumber);

        Customer customer = customerRepository.findByCustomerNumber(customerNumber)
                .orElseThrow(() -> new CustomerNotFoundException(
                        "Customer not found with number: " + customerNumber
                ));

        return customerMapper.toInternalDto(customer);
    }

    /**
     * Get all customers with summary information
     *
     * WHY: For list views, we don't need full details.
     * This is more efficient and secure.
     */
    public List<CustomerSummaryDto> getAllCustomers() {
        log.debug("Fetching all customers (summary)");

        List<Customer> customers = customerRepository.findAll();
        return customerMapper.toSummaryDtoList(customers);
    }

    /**
     * Get all customers with full details (admin only)
     *
     * WHY: Sometimes we need full details (reports, admin panels).
     * This would be secured with admin authorization.
     */
    public List<CustomerResponseDto> getAllCustomersFull() {
        log.debug("Fetching all customers (full details)");

        return customerRepository.findAll().stream()
                .map(customerMapper::toResponseDto)
                .toList();
    }

    /**
     * Update customer information
     *
     * WHY: Updates an existing customer with new data.
     *
     * NOTE: We use a separate method that only updates allowed fields.
     * This prevents clients from updating:
     * - customerNumber (business key)
     * - status (controlled by the service)
     * - audit fields (managed by the service)
     */
    @Transactional
    public CustomerResponseDto updateCustomer(String customerNumber, CustomerRequestDto requestDto) {
        log.info("Updating customer with number: {}", customerNumber);

        Customer existingCustomer = customerRepository.findByCustomerNumber(customerNumber)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with number: " + customerNumber));

        customerMapper.updateEntityFromRequest(requestDto, existingCustomer);

        existingCustomer.setUpdatedBy("SYSTEM");

        Customer updatedCustomer = customerRepository.save(existingCustomer);

        log.info("Customer updated successfully with ID: {}", updatedCustomer.getId());

        return customerMapper.toResponseDto(updatedCustomer);
    }

    /**
     * Update customer status (internal use only)
     *
     * WHY: Status changes are controlled by the service.
     * For example:
     * - After KYC completes: PENDING_VERIFICATION → ACTIVE
     * - After fraud detection: ACTIVE → BLOCKED
     * - After account closure: ACTIVE → INACTIVE
     *
     * SECURITY: This would be an internal endpoint, not exposed to clients.
     */
    @Transactional
    public CustomerResponseDto updateCustomerStatus(String customerNumber, String status) {
        log.info("Updating customer status for number: {} to: {}", customerNumber, status);

        Customer customer = customerRepository.findByCustomerNumber(customerNumber)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with number: " + customerNumber));

        if (!isValidStatus(status)) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }

        customer.setStatus(status);
        customer.setUpdatedBy("SYSTEM");

        Customer updatedCustomer = customerRepository.save(customer);

        return customerMapper.toResponseDto(updatedCustomer);
    }

    /**
     * Delete a customer (soft delete)
     *
     * WHY: We don't actually delete customers in banking.
     * We deactivate them (set status to INACTIVE) and maintain
     * the data for audit and regulatory compliance.
     */
    @Transactional
    public void deactivateCustomer(String customerNumber) {
        log.warn("Deactivating customer with number: {}", customerNumber);

        Customer customer = customerRepository.findByCustomerNumber(customerNumber)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with number: " + customerNumber));

        customer.setStatus("INACTIVE");
        customer.setUpdatedBy("SYSTEM");

        customerRepository.save(customer);

        log.warn("Customer deactivated with number: {}", customerNumber);
    }

    private boolean isValidStatus(String status) {
        return List.of("ACTIVE", "INACTIVE", "BLOCKED", "PENDING_VERIFICATION").contains(status);
    }

    /**
     * Generate a customer number
     *
     * WHY: Business key generation is the service's responsibility.
     * In a real bank, customer numbers have specific formats
     * and might include checksums.
     */
    public String generateCustomerNumber() {
        // Simple generation for learning purposes
        // In a real bank, this would be more sophisticated
        return "CUST-" + String.format("%06d",
                (long) (Math.random() * 1000000));
    }

    /**
     * List customers with pagination, filtering, and sorting
     */
    public Page<CustomerSummaryDto> listCustomers(Pageable pageable, String status, String search) {
        // Build the specification
        Specification<Customer> spec = Specification.where(null);

        if (status != null && !status.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), status));
        }

        if (search != null && !search.isEmpty()) {
            String searchPattern = "%" + search.toLowerCase() + "%";
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("firstName")), searchPattern),
                            cb.like(cb.lower(root.get("lastName")), searchPattern),
                            cb.like(cb.lower(root.get("email")), searchPattern)
                    ));
        }

        Page<Customer> customers = customerRepository.findAll(spec, pageable);
        return customers.map(customerMapper::toSummaryDto);
    }

    /**
     * Advanced search
     */
    public Page<CustomerSummaryDto> searchCustomers(Pageable pageable, SearchRequest searchRequest) {
        Specification<Customer> spec = Specification.where(null);

        if (searchRequest.getFirstName() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(cb.lower(root.get("firstName")),
                            searchRequest.getFirstName().toLowerCase()));
        }

        if (searchRequest.getLastName() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(cb.lower(root.get("lastName")),
                            searchRequest.getLastName().toLowerCase()));
        }

        if (searchRequest.getEmail() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(cb.lower(root.get("email")),
                            searchRequest.getEmail().toLowerCase()));
        }

        if (searchRequest.getStatuses() != null && !searchRequest.getStatuses().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    root.get("status").in(searchRequest.getStatuses()));
        }

        if (searchRequest.getDateOfBirthFrom() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("dateOfBirth"),
                            searchRequest.getDateOfBirthFrom()));
        }

        if (searchRequest.getDateOfBirthTo() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("dateOfBirth"),
                            searchRequest.getDateOfBirthTo()));
        }

        if (searchRequest.getSearchTerm() != null && !searchRequest.getSearchTerm().isEmpty()) {
            String pattern = "%" + searchRequest.getSearchTerm().toLowerCase() + "%";
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("firstName")), pattern),
                            cb.like(cb.lower(root.get("lastName")), pattern),
                            cb.like(cb.lower(root.get("email")), pattern)
                    ));
        }

        Page<Customer> customers = customerRepository.findAll(spec, pageable);
        return customers.map(customerMapper::toSummaryDto);
    }

    /**
     * Get customer addresses
     */
    public List<AddressDto> getCustomerAddresses(String customerNumber) {
        Customer customer = customerRepository.findByCustomerNumber(customerNumber)
                .orElseThrow(() -> new CustomerNotFoundException(
                        "Customer not found with number: " + customerNumber));

        return customer.getAddresses().stream()
                .map(addressMapper::toDto)
                .toList();
    }

    /**
     * Add customer address
     */
    @Transactional
    public AddressDto addAddress(String customerNumber, AddressRequestDto addressDto) {
        Customer customer = customerRepository.findByCustomerNumber(customerNumber)
                .orElseThrow(() -> new CustomerNotFoundException(
                        "Customer not found with number: " + customerNumber));

        Address address = addressMapper.toEntity(addressDto);
        address.setCustomer(customer);
        address.setCreatedBy("SYSTEM");
        address.setUpdatedBy("SYSTEM");

        // If this is primary, unset other primary addresses
        if (addressDto.isPrimary()) {
            customer.getAddresses().forEach(a -> a.setIsPrimary(false));
        }

        customer.getAddresses().add(address);
        Customer saved = customerRepository.save(customer);

        return addressMapper.toDto(
                saved.getAddresses().stream()
                        .filter(a -> a.getStreet().equals(addressDto.getStreet()))
                        .findFirst()
                        .orElseThrow()
        );
    }

    /**
     * Update customer address
     */
    @Transactional
    public AddressDto updateAddress(String customerNumber, Long addressId, AddressRequestDto addressDto) {
        Customer customer = customerRepository.findByCustomerNumber(customerNumber)
                .orElseThrow(() -> new CustomerNotFoundException(
                        "Customer not found with number: " + customerNumber));

        Address address = customer.getAddresses().stream()
                .filter(a -> a.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Address not found"));

        addressMapper.updateEntity(addressDto, address);
        address.setUpdatedBy("SYSTEM");

        if (addressDto.isPrimary()) {
            customer.getAddresses().forEach(a -> {
                if (!a.getId().equals(addressId)) {
                    a.setIsPrimary(false);
                }
            });
        }

        customerRepository.save(customer);
        return addressMapper.toDto(address);
    }

    /**
     * Delete customer address
     */
    @Transactional
    public void deleteAddress(String customerNumber, Long addressId) {
        Customer customer = customerRepository.findByCustomerNumber(customerNumber)
                .orElseThrow(() -> new CustomerNotFoundException(
                        "Customer not found with number: " + customerNumber));

        Address address = customer.getAddresses().stream()
                .filter(a -> a.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Address not found"));

        customer.getAddresses().remove(address);
        customerRepository.save(customer);
    }
}