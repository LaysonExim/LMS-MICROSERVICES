// Purpose: Mapper for Collateral entities and DTOs
// File: collateral-service/src/main/java/com/nextgenloan/collateral/mapper/CollateralMapper.java
// Dependencies: MapStruct

package com.nextgenloan.collateral_service.mapper;

import com.nextgenloan.collateral_service.dto.CollateralResponseDto;
import com.nextgenloan.collateral_service.dto.CollateralResponseDto.ValuationDto;
import com.nextgenloan.collateral_service.entity.CollateralAsset;
import com.nextgenloan.collateral_service.entity.CollateralValuation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CollateralMapper {

    CollateralMapper INSTANCE = Mappers.getMapper(CollateralMapper.class);

    @Mapping(target = "collateralReference", source = "collateralReference")
    @Mapping(target = "customerNumber", source = "customerNumber")
    @Mapping(target = "assetType", source = "assetType")
    @Mapping(target = "assetName", source = "assetName")
    @Mapping(target = "assetDescription", source = "assetDescription")
    @Mapping(target = "valuation", source = "valuation")
    @Mapping(target = "valuationDate", source = "valuationDate")
    @Mapping(target = "currency", source = "currency")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "loanNumber", source = "loanNumber")
    @Mapping(target = "pledgeDate", source = "pledgeDate")
    @Mapping(target = "releaseDate", source = "releaseDate")
    @Mapping(target = "legalStatus", source = "legalStatus")
    @Mapping(target = "insuranceStatus", source = "insuranceStatus")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "updatedBy", source = "updatedBy")
    @Mapping(target = "version", source = "version")
    @Mapping(target = "valuations", source = "valuations")
    CollateralResponseDto toResponseDto(CollateralAsset collateralAsset);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "valuationDate", source = "valuationDate")
    @Mapping(target = "valuation", source = "valuation")
    @Mapping(target = "valuationType", source = "valuationType")
    @Mapping(target = "appraiser", source = "appraiser")
    @Mapping(target = "notes", source = "notes")
    @Mapping(target = "createdAt", source = "createdAt")
    ValuationDto toValuationDto(CollateralValuation collateralValuation);

    List<CollateralResponseDto> toResponseDtoList(List<CollateralAsset> collateralAssets);
}
