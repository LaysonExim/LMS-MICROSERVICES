// Purpose: Mapper for Loan entities and DTOs
// File: loan-service/src/main/java/com/nextgenloan/loan/mapper/LoanMapper.java

package com.nextgenloan.loan_service.mapper;

import com.nextgenloan.loan_service.dto.LoanRepaymentDto;
import com.nextgenloan.loan_service.dto.LoanRequestDto;
import com.nextgenloan.loan_service.dto.LoanResponseDto;
import com.nextgenloan.loan_service.dto.LoanScheduleDto;
import com.nextgenloan.loan_service.entity.LoanApplication;
import com.nextgenloan.loan_service.entity.LoanRepayment;
import com.nextgenloan.loan_service.entity.LoanSchedule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LoanMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "loanNumber", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "applicationDate", ignore = true)
    @Mapping(target = "approvedDate", ignore = true)
    @Mapping(target = "disbursementDate", ignore = true)
    @Mapping(target = "closureDate", ignore = true)
    @Mapping(target = "firstName", ignore = true)
    @Mapping(target = "lastName", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "schedules", ignore = true)
    @Mapping(target = "repayments", ignore = true)
    LoanApplication toEntity(LoanRequestDto requestDto);

    @Mapping(target = "schedules", source = "schedules")
    @Mapping(target = "repayments", source = "repayments")
    LoanResponseDto toResponseDto(LoanApplication loan);

    List<LoanResponseDto> toResponseDtoList(List<LoanApplication> loans);

    LoanScheduleDto toScheduleDto(LoanSchedule schedule);

    List<LoanScheduleDto> toScheduleDtoList(List<LoanSchedule> schedules);

    LoanRepaymentDto toRepaymentDto(LoanRepayment repayment);

    List<LoanRepaymentDto> toRepaymentDtoList(List<LoanRepayment> repayments);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "loanNumber", ignore = true)
    @Mapping(target = "customerNumber", ignore = true)
    @Mapping(target = "loanType", ignore = true)
    @Mapping(target = "loanPurpose", ignore = true)
    @Mapping(target = "amount", ignore = true)
    @Mapping(target = "interestRate", ignore = true)
    @Mapping(target = "termMonths", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "applicationDate", ignore = true)
    @Mapping(target = "approvedDate", ignore = true)
    @Mapping(target = "disbursementDate", ignore = true)
    @Mapping(target = "closureDate", ignore = true)
    @Mapping(target = "firstName", ignore = true)
    @Mapping(target = "lastName", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "schedules", ignore = true)
    @Mapping(target = "repayments", ignore = true)
    void updateEntityFromRequest(LoanRequestDto requestDto, @MappingTarget LoanApplication target);
}