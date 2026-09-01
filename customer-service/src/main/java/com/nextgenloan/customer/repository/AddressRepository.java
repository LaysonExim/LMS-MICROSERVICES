// Purpose: Repository for Address entity

package com.nextgenloan.customer.repository;

import com.nextgenloan.customer.entity.Address;
import com.nextgenloan.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByCustomer(Customer customer);

    List<Address> findByCustomerAndIsPrimaryTrue(Customer customer);

    boolean existsByCustomerAndAddressType(Customer customer, String addressType);
}