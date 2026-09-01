// Purpose: Mapper for Credit Limit entities and DTOs
// File: limit-service/src/main/java/com/nextgenloan/limit/mapper/CreditLimitMapper.java
// Dependencies: MapStruct

package com.nextgenloan.limit_service.mapper;

import com.nextgenloan.limit_service.dto.CreditLimitResponseDto;
import com.nextgenloan.limit_service.dto.LimitCheckResponseDto;
import com.nextgenloan.limit_service.entity.CustomerCreditLimit;
import com.nextgenloan.limit_service.entity.ProductCreditLimit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CreditLimitMapper {

    CreditLimitMapper INSTANCE = Mappers.getMapper(CreditLimitMapper.class);

    @Mapping(target = "customerNumber", source = "customerNumber")
    @Mapping(target = "totalLimit", source = "totalLimit")
    @Mapping(target = "usedLimit", source = "usedLimit")
    @Mapping(target = "availableLimit", source = "availableLimit")
    @Mapping(target = "currency", source = "currency")
    @Mapping(target = "riskScore", source = "riskScore")
    @Mapping(target = "creditRating", source = "creditRating")
    @Mapping(target = "productLimits", source = "productLimits")
    CreditLimitResponseDto toResponseDto(CustomerCreditLimit customerCreditLimit);

    @Mapping(target = "productType", source = "productType")
    @Mapping(target = "limitAmount", source = "limitAmount")
    @Mapping(target = "usedAmount", source = "usedAmount")
    @Mapping(target = "availableAmount", source = "availableAmount")
    CreditLimitResponseDto.ProductLimitDto toProductLimitDto(ProductCreditLimit productCreditLimit);
}
