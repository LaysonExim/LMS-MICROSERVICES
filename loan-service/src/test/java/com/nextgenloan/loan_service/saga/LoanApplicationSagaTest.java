// Purpose: Test saga behavior
// File: loan-service/src/test/java/com/nextgenloan/loan/saga/LoanApplicationSagaTest.java

package com.nextgenloan.loan_service.saga;

import com.nextgenloan.loan_service.client.CustomerServiceClient;
import com.nextgenloan.loan_service.dto.LoanRequestDto;
import com.nextgenloan.loan_service.dto.LoanResponseDto;
import com.nextgenloan.loan_service.exception.InvalidLoanOperationException;
import com.nextgenloan.loan_service.model.LoanState;
import com.nextgenloan.loan_service.service.LoanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanApplicationSagaTest {

    @Mock
    private LoanService loanService;

    @Mock
    private CustomerServiceClient customerServiceClient;

    @InjectMocks
    private LoanApplicationSaga saga;

    private LoanRequestDto requestDto;
    private LoanResponseDto responseDto;

    @BeforeEach
    void setUp() {
        requestDto = LoanRequestDto.builder()
                .customerNumber("CUST-001")
                .loanType("PERSONAL")
                .amount(BigDecimal.valueOf(10000))
                .interestRate(BigDecimal.valueOf(5.5))
                .termMonths(36)
                .build();

        responseDto = LoanResponseDto.builder()
                .loanNumber("LN-001")
                .customerNumber("CUST-001")
                .status(LoanState.PENDING.getCode())
                .build();
    }

    @Test
    void shouldExecuteSagaSuccessfully() {
        // Given
        when(loanService.createLoanApplication(any())).thenReturn(responseDto);
        when(loanService.transitionState(anyString(), anyString(), anyString()))
                .thenReturn(responseDto);
        when(customerServiceClient.validateCustomer(anyString()))
                .thenReturn(Mono.just(true));

        // When
        LoanResponseDto result = saga.executeSaga(requestDto).block();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getLoanNumber()).isEqualTo("LN-001");
        verify(loanService).createLoanApplication(any());
        verify(customerServiceClient).validateCustomer(anyString());
        verify(loanService).transitionState(anyString(), eq("VERIFIED"), anyString());
    }

    @Test
    void shouldCancelLoanOnCustomerValidationFailure() {
        // Given
        when(loanService.createLoanApplication(any())).thenReturn(responseDto);
        when(customerServiceClient.validateCustomer(anyString()))
                .thenReturn(Mono.just(false));

        // When
        Throwable thrown = null;
        try {
            saga.executeSaga(requestDto).block();
        } catch (Exception e) {
            thrown = e;
        }

        // Then
        assertThat(thrown).isInstanceOf(InvalidLoanOperationException.class);
        verify(loanService).createLoanApplication(any());
        verify(loanService).transitionState(eq("LN-001"), eq("REJECTED"), anyString());
    }
}