// Purpose: Mapper for Address entity and DTOs
// Dependencies: MapStruct

package com.nextgenloan.customer.mapper;

import com.nextgenloan.customer.dto.AddressDto;
import com.nextgenloan.customer.dto.AddressRequestDto;
import com.nextgenloan.customer.entity.Address;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "addressType", source = "addressType")
    @Mapping(target = "street", source = "street")
    @Mapping(target = "city", source = "city")
    @Mapping(target = "state", source = "state")
    @Mapping(target = "postalCode", source = "postalCode")
    @Mapping(target = "country", source = "country")
    @Mapping(target = "isPrimary", source = "isPrimary")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    AddressDto toDto(Address address);

    List<AddressDto> toDtoList(List<Address> addresses);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Address toEntity(AddressRequestDto addressDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntity(AddressRequestDto addressDto, @MappingTarget Address address);
}