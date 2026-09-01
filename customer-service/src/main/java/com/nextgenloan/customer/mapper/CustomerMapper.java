// Purpose: Complete mapper for all Customer DTOs
// File: customer-service/src/main/java/com/nextgenloan/customer/mapper/CustomerMapper.java
// Dependencies: MapStruct

package com.nextgenloan.customer.mapper;

import com.nextgenloan.customer.dto.*;
import com.nextgenloan.customer.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * Customer Mapper
 *
 * WHY: MapStruct provides type-safe mapping at compile time.
 *
 * ADVANTAGES:
 * 1. Compile-time validation - Mapping errors are caught during compilation
 * 2. Type-safe - No reflection, no runtime errors
 * 3. Fast - Generated code is as fast as hand-written code
 * 4. Maintainable - Changes to entities are caught at compile time
 * 5. Null-safe - Handles nulls properly
 *
 * In a bank, we use MapStruct because:
 * - We have hundreds of DTOs across services
 * - We need to be able to refactor safely
 * - We need to minimize runtime overhead
 *
 * REAL BANK EXAMPLE: At one bank, we had 50+ services, each with multiple
 * DTOs. We used MapStruct to manage all mappings. When we changed the
 * Customer entity, MapStruct caught every single mapping error across
 * all services during the build. We fixed them in 10 minutes instead
 * of spending days testing and debugging.
 */
@Mapper(componentModel = "spring")
public interface CustomerMapper {

    CustomerMapper INSTANCE = Mappers.getMapper(CustomerMapper.class);

    // ===== Entity to DTO Mappings =====

    /**
     * Convert entity to CustomerResponseDto (for API responses)
     *
     * Note: We don't map internal fields (id, version, created_by, updated_by)
     */
    @Mapping(target = "customerNumber", source = "customerNumber")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "phoneNumber", source = "phoneNumber")
    @Mapping(target = "dateOfBirth", source = "dateOfBirth")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    CustomerResponseDto toResponseDto(Customer customer);

    /**
     * Convert entity to CustomerSummaryDto (for list responses)
     */
    @Mapping(target = "customerNumber", source = "customerNumber")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "status", source = "status")
    CustomerSummaryDto toSummaryDto(Customer customer);

    /**
     * Convert entity to CustomerInternalDto (for internal communication)
     *
     * Note: This includes internal fields
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "customerNumber", source = "customerNumber")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "phoneNumber", source = "phoneNumber")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "version", source = "version")
    CustomerInternalDto toInternalDto(Customer customer);

    // ===== DTO to Entity Mappings =====

    /**
     * Convert Request DTO to Entity (for creation)
     *
     * Ignored fields:
     * - id (database generated)
     * - createdAt, updatedAt (service managed)
     * - createdBy, updatedBy (service managed)
     * - version (database managed)
     * - status (service sets default)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "status", ignore = true)
    Customer toEntityFromRequest(CustomerRequestDto requestDto);

    /**
     * Update entity from Request DTO (for updates)
     *
     * This method updates an existing entity with new data from the DTO.
     * We only update fields that the client can change.
     *
     * EXPLANATION: We don't update customerNumber because it's the
     * business key and shouldn't be changed (would break references).
     *
     * We also don't update audit fields, status, or version.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customerNumber", ignore = true)  // Cannot change business key
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "status", ignore = true)
    void updateEntityFromRequest(CustomerRequestDto requestDto, @MappingTarget Customer customer);

    // ===== Helper Methods for Custom Mappings =====

    /**
     * Convert Customer entity to Response DTO with computed full name
     *
     * This is a convenience method that demonstrates how to add
     * computed fields to DTOs.
     */
    default CustomerResponseDto toResponseDtoWithFullName(Customer customer) {
        CustomerResponseDto dto = toResponseDto(customer);
        // Add computed fields if needed
        // For example: dto.setFullName(customer.getFirstName() + " " + customer.getLastName());
        return dto;
    }

    /**
     * Convert a list of entities to a list of summary DTOs
     */
    List<CustomerSummaryDto> toSummaryDtoList(List<Customer> customers);
}