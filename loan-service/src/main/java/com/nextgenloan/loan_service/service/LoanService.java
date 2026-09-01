// Purpose: Complete Loan Service with all integrations
// File: loan-service/src/main/java/com/nextgenloan/loan/service/LoanService.java

package com.nextgenloan.loan_service.service;

import com.nextgenloan.loan_service.client.CollateralClient;
import com.nextgenloan.loan_service.client.CreditLimitClient;
import com.nextgenloan.loan_service.client.CustomerServiceClient;
import com.nextgenloan.loan_service.dto.*;
import com.nextgenloan.loan_service.entity.LoanApplication;
import com.nextgenloan.loan_service.entity.LoanRepayment;
import com.nextgenloan.loan_service.entity.LoanSchedule;
import com.nextgenloan.loan_service.exception.InvalidLoanOperationException;
import com.nextgenloan.loan_service.exception.LoanNotFoundException;
import com.nextgenloan.loan_service.exception.LoanStateTransitionException;
import com.nextgenloan.loan_service.mapper.LoanMapper;
import com.nextgenloan.loan_service.model.LoanState;
import com.nextgenloan.loan_service.repository.LoanRepository;
import com.nextgenloan.loan_service.repository.LoanScheduleRepository;
import com.nextgenloan.loan_service.repository.LoanRepaymentRepository;
import com.nextgenloan.loan_service.event.LoanEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanService {

    private final LoanRepository loanRepository;
    private final LoanScheduleRepository scheduleRepository;
    private final LoanRepaymentRepository repaymentRepository;
    private final LoanMapper loanMapper;
    private final CustomerServiceClient customerServiceClient;
    private final CreditLimitClient creditLimitClient;
    private final CollateralClient collateralClient;
    private final LoanEventPublisher eventPublisher;

    /**
     * Apply for a new loan - COMPLETE FLOW
     * 
     * WHY: This is the full loan application process with all integrations.
     * 
     * STEPS:
     * 1. Validate customer (Customer Service)
     * 2. Check credit limit (Credit Limit Service)
     * 3. Validate collateral (Collateral Service)
     * 4. Create loan application
     * 5. Generate schedule
     * 6. Confirm credit reservation
     * 7. Link collateral to loan
     * 8. Return response
     * 
     * This demonstrates complete synchronous communication
     * between all services in our platform.
     */
    @Transactional
    public Mono<LoanResponseDto> applyForLoanComplete(LoanRequestDto requestDto) {
        log.info("Processing complete loan application for customer: {}", 
            requestDto.getCustomerNumber());

        return customerServiceClient.getCustomer(requestDto.getCustomerNumber())
            .flatMap(customer -> {
                // Step 1: Validate customer
                if (customer == null || "UNKNOWN".equals(customer.getStatus())) {
                    log.error("Invalid customer: {}", requestDto.getCustomerNumber());
                    return Mono.error(new InvalidLoanOperationException(
                        "Customer not found or invalid: " + requestDto.getCustomerNumber()
                    ));
                }
                log.info("Customer validated: {}", customer.getCustomerNumber());

                // Step 2: Check credit limit
                CreditLimitCheckRequest limitRequest = CreditLimitCheckRequest.builder()
                    .customerNumber(requestDto.getCustomerNumber())
                    .productType(requestDto.getLoanType())
                    .amount(requestDto.getAmount())
                    .loanNumber(null)  // Will be generated later
                    .build();

                return creditLimitClient.checkAndReserveCredit(limitRequest)
                    .flatMap(limitResponse -> {
                        if (!limitResponse.isApproved()) {
                            log.warn("Credit limit check failed: {}", 
                                limitResponse.getMessage());
                            return Mono.error(new InvalidLoanOperationException(
                                "Insufficient credit: " + limitResponse.getMessage()
                            ));
                        }
                        log.info("Credit reserved: {}", limitResponse.getReservationId());

                        // Step 3: Validate collateral
                        CollateralValidationRequest collateralRequest = 
                            CollateralValidationRequest.builder()
                                .customerNumber(requestDto.getCustomerNumber())
                                .loanNumber(limitResponse.getLoanNumber())
                                .loanAmount(requestDto.getAmount())
                                .build();

                        return collateralClient.validateCollateral(collateralRequest)
                            .flatMap(collateralResponse -> {
                                if (!collateralResponse.isValid()) {
                                    // If collateral fails, release credit reservation
                                    creditLimitClient.releaseReservation(
                                        limitResponse.getReservationId()
                                    ).subscribe();
                                    
                                    log.warn("Collateral validation failed: {}", 
                                        collateralResponse.getMessage());
                                    return Mono.error(new InvalidLoanOperationException(
                                        "Insufficient collateral: " + 
                                        collateralResponse.getMessage()
                                    ));
                                }
                                log.info("Collateral validated: {}", 
                                    collateralResponse.getTotalValue());

                                // Step 4: Create loan application
                                LoanApplication loan = createLoanApplication(
                                    requestDto, customer
                                );

                                // Step 5: Generate schedule
                                List<LoanSchedule> schedules = 
                                    generateAmortizationSchedule(loan);
                                loan.setSchedules(schedules);

                                // Save loan
                                LoanApplication savedLoan = loanRepository.save(loan);
                                log.info("Loan created: {}", savedLoan.getLoanNumber());

                                // Publish loan created event
                                eventPublisher.publishLoanCreatedEvent(loanMapper.toResponseDto(savedLoan));

                                // Step 6: Confirm credit reservation
                                creditLimitClient.confirmReservation(
                                    limitResponse.getReservationId()
                                ).subscribe();

                                // Step 7: Link collateral to loan
                                // Note: In a real system, we would link specific collateral
                                // For simplicity, we link all active collateral
                                collateralClient.linkCollateralToLoan(
                                    savedLoan.getLoanNumber()
                                ).subscribe();

                                log.info("Loan application completed: {}", 
                                    savedLoan.getLoanNumber());

                                return Mono.just(loanMapper.toResponseDto(savedLoan));
                            });
                    });
            });
    }

    /**
     * Create loan application entity with customer data
     */
    private LoanApplication createLoanApplication(LoanRequestDto requestDto, 
                                                   CustomerDto customer) {
        String loanNumber = generateLoanNumber();

        return LoanApplication.builder()
            .loanNumber(loanNumber)
            .customerNumber(requestDto.getCustomerNumber())
            .loanType(requestDto.getLoanType())
            .loanPurpose(requestDto.getLoanPurpose())
            .amount(requestDto.getAmount())
            .interestRate(requestDto.getInterestRate())
            .termMonths(requestDto.getTermMonths())
            .status(LoanState.PENDING.getCode())
            .applicationDate(LocalDateTime.now())
            .firstName(customer.getFirstName())
            .lastName(customer.getLastName())
            .email(customer.getEmail())
            .createdBy("SYSTEM")
            .updatedBy("SYSTEM")
            .build();
    }

    /**
     * Apply for a new loan
     *
     * WHY: This is the entry point for loan applications.
     *
     * PROCESS:
     * 1. Validate customer exists and is active
     * 2. Validate loan parameters (amount, term, etc.)
     * 3. Generate loan number
     * 4. Create loan application
     * 5. Generate repayment schedule
     * 6. Save loan with schedule
     * 7. Return loan details
     *
     * In a real bank, this would also:
     * - Check customer's existing loans
     * - Check credit limit
     * - Perform fraud checks
     * - Run affordability checks
     * - Trigger workflow for approval
     */
    @Transactional
    public Mono<LoanResponseDto> applyForLoan(LoanRequestDto requestDto) {
        log.info("Processing loan application for customer: {}", requestDto.getCustomerNumber());

        // Step 1: Validate customer
        return customerServiceClient.validateCustomer(requestDto.getCustomerNumber())
                .flatMap(isValid -> {
                    if (!isValid) {
                        log.error("Invalid customer: {}", requestDto.getCustomerNumber());
                        return Mono.error(new InvalidLoanOperationException(
                                "Customer is not valid for loan application: " + requestDto.getCustomerNumber()
                        ));
                    }

                    // Step 2: Validate loan parameters
                    validateLoanParameters(requestDto);

                    // Step 3: Create loan application
                    return createLoanApplicationWithCustomerValidation(requestDto)
                            .map(loan -> {
                                List<LoanSchedule> schedules = generateAmortizationSchedule(loan);
                                loan.setSchedules(schedules);
                                return loan;
                            })
                            .map(loanRepository::save)
                            .map(savedLoan -> {
                                log.info("Loan application created: {}", savedLoan.getLoanNumber());
                                return loanMapper.toResponseDto(savedLoan);
                            });
                });
    }

    /**
     * Validate loan parameters
     *
     * WHY: We need to ensure loans meet minimum requirements.
     *
     * In a real bank, this would include:
     * - Minimum loan amount
     * - Maximum loan amount
     * - Minimum term
     * - Maximum term
     * - Valid loan types
     * - Interest rate limits
     */
    private void validateLoanParameters(LoanRequestDto requestDto) {
        // Basic validation
        if (requestDto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidLoanOperationException("Loan amount must be greater than zero");
        }

        if (requestDto.getInterestRate().compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidLoanOperationException("Interest rate cannot be negative");
        }

        if (requestDto.getTermMonths() <= 0) {
            throw new InvalidLoanOperationException("Loan term must be at least 1 month");
        }

        if (requestDto.getTermMonths() > 360) { // 30 years
            throw new InvalidLoanOperationException("Loan term cannot exceed 360 months (30 years)");
        }

        // In a real bank, we'd have more validation:
        // - Check against loan product rules
        // - Check regulatory limits
        // - Check risk-based pricing
        // - Check affordability
        log.debug("Loan parameters validated successfully");
    }

    /**
     * Create loan application entity (for saga)
     *
     * WHY: Convert DTO to entity with proper defaults.
     *
     * We set:
     * - Loan number (business key)
     * - Status (PENDING)
     * - Application date (today)
     * - Customer details (from Customer Service)
     */
    private Mono<LoanApplication> createLoanApplicationWithCustomerValidation(LoanRequestDto requestDto) {
        String loanNumber = generateLoanNumber();

        // Fetch customer details to populate denormalized fields
        return customerServiceClient.getCustomer(requestDto.getCustomerNumber())
                .map(customer -> {
                    CustomerDto cust = customer != null ? customer : null;
                    String firstName = cust != null ? cust.getFirstName() : "Unknown";
                    String lastName = cust != null ? cust.getLastName() : "Customer";
                    String email = cust != null ? cust.getEmail() : "unknown@bank.com";

                    return LoanApplication.builder()
                            .loanNumber(loanNumber)
                            .customerNumber(requestDto.getCustomerNumber())
                            .loanType(requestDto.getLoanType())
                            .loanPurpose(requestDto.getLoanPurpose())
                            .amount(requestDto.getAmount())
                            .interestRate(requestDto.getInterestRate())
                            .termMonths(requestDto.getTermMonths())
                            .status(LoanState.PENDING.getCode())
                            .applicationDate(LocalDateTime.now())
                            .firstName(firstName)
                            .lastName(lastName)
                            .email(email)
                            .createdBy("SYSTEM")
                            .updatedBy("SYSTEM")
                            .build();
                });
    }

    /**
     * Create loan application (for saga)
     *
     * WHY: This is a simplified version for the saga.
     * It creates the loan without customer validation.
     *
     * This is safe because the loan is created in PENDING state.
     * If the saga fails, the loan can be cancelled.
     */
    @Transactional
    public LoanResponseDto createLoanApplication(LoanRequestDto requestDto) {
        log.info("Creating loan application (saga mode) for customer: {}", 
            requestDto.getCustomerNumber());
        
        // Validate loan parameters
        validateLoanParameters(requestDto);
        
        // Create loan application
        LoanApplication loan = createLoanApplicationEntity(requestDto);
        
        // Generate schedule
        List<LoanSchedule> schedules = generateAmortizationSchedule(loan);
        loan.setSchedules(schedules);
        
        // Save loan
        LoanApplication savedLoan = loanRepository.save(loan);
        log.info("Loan application created: {}", savedLoan.getLoanNumber());
        eventPublisher.publishLoanCreatedEvent(loanMapper.toResponseDto(savedLoan));

        return loanMapper.toResponseDto(savedLoan);
    }
    
    /**
     * Create loan application entity (for saga)
     */
    private LoanApplication createLoanApplicationEntity(LoanRequestDto requestDto) {
        String loanNumber = generateLoanNumber();
        
        // For saga, we use default customer info
        // The actual verification happens in the saga
        LoanApplication loan = LoanApplication.builder()
            .loanNumber(loanNumber)
            .customerNumber(requestDto.getCustomerNumber())
            .loanType(requestDto.getLoanType())
            .loanPurpose(requestDto.getLoanPurpose())
            .amount(requestDto.getAmount())
            .interestRate(requestDto.getInterestRate())
            .termMonths(requestDto.getTermMonths())
            .status(LoanState.PENDING.getCode())
            .applicationDate(LocalDateTime.now())
            .firstName("Pending")  // Will be updated when customer is verified
            .lastName("Verification")
            .email("pending@bank.com")
            .createdBy("SYSTEM")
            .updatedBy("SYSTEM")
            .build();
        
        return loan;
    }

    /**
     * Generate amortization schedule
     *
     * WHY: The repayment schedule is the heart of the loan.
     * It shows when payments are due and how much.
     *
     * FORMULA:
     * Payment = P * r * (1+r)^n / ((1+r)^n - 1)
     * Where:
     * P = Principal (loan amount)
     * r = Monthly interest rate (annual rate / 12)
     * n = Number of payments (term in months)
     *
     * This is the standard amortization formula used in banking.
     *
     * For each payment:
     * - Interest = Previous balance * r
     * - Principal = Payment - Interest
     * - Balance = Previous balance - Principal
     *
     * In a real bank, we'd handle:
     * - Different payment frequencies
     * - Different interest calculation methods
     * - Fees and penalties
     * - Variable interest rates
     */
    private List<LoanSchedule> generateAmortizationSchedule(LoanApplication loan) {
        log.info("Generating amortization schedule for loan: {}", loan.getLoanNumber());

        List<LoanSchedule> schedules = new ArrayList<>();

        BigDecimal principal = loan.getAmount();
        BigDecimal annualRate = loan.getInterestRate().divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);
        int months = loan.getTermMonths();

        // Calculate monthly payment using amortization formula
        // Payment = P * r * (1+r)^n / ((1+r)^n - 1)
        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
        BigDecimal onePlusRPowerN = onePlusR.pow(months);

        BigDecimal numerator = principal.multiply(monthlyRate).multiply(onePlusRPowerN);
        BigDecimal denominator = onePlusRPowerN.subtract(BigDecimal.ONE);

        BigDecimal monthlyPayment = numerator.divide(denominator, 2, RoundingMode.HALF_UP);

        BigDecimal remainingBalance = principal;

        for (int i = 1; i <= months; i++) {
            // Calculate interest for this period
            BigDecimal interest = remainingBalance.multiply(monthlyRate)
                    .setScale(2, RoundingMode.HALF_UP);

            // Calculate principal for this period
            BigDecimal principalPayment = monthlyPayment.subtract(interest)
                    .setScale(2, RoundingMode.HALF_UP);

            // Calculate new balance
            BigDecimal newBalance = remainingBalance.subtract(principalPayment)
                    .setScale(2, RoundingMode.HALF_UP);

            // Handle rounding differences (last payment)
            if (i == months) {
                // Ensure the last payment pays off the entire balance
                principalPayment = remainingBalance;
                newBalance = BigDecimal.ZERO;
                monthlyPayment = principalPayment.add(interest).setScale(2, RoundingMode.HALF_UP);
            }

            // Create schedule entry
            LoanSchedule schedule = LoanSchedule.builder()
                    .loan(loan)
                    .installmentNumber(i)
                    .dueDate(LocalDate.now().plusMonths(i))
                    .installmentAmount(monthlyPayment)
                    .principalAmount(principalPayment)
                    .interestAmount(interest)
                    .balanceAfterInstallment(newBalance)
                    .status("PENDING")
                    .build();

            schedules.add(schedule);

            remainingBalance = newBalance;
        }

        log.info("Generated {} schedule entries for loan: {}", schedules.size(), loan.getLoanNumber());
        return schedules;
    }

    /**
     * Generate unique loan number
     *
     * WHY: Loans need a unique business key.
     *
     * In a real bank, loan numbers:
     * - Follow a specific format
     * - Include branch/region codes
     * - Include checksum digits
     * - Are sequential (with a specific algorithm)
     * - Are globally unique across systems
     */
    private String generateLoanNumber() {
        // Simple generation for learning
        // Format: LN-YYYYMMDD-XXXX
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String sequence = String.format("%04d", (long) (Math.random() * 10000));
        String loanNumber = "LN-" + date + "-" + sequence;

        // Ensure uniqueness
        while (loanRepository.existsByLoanNumber(loanNumber)) {
            sequence = String.format("%04d", (long) (Math.random() * 10000));
            loanNumber = "LN-" + date + "-" + sequence;
        }

        return loanNumber;
    }

    /**
     * Transition loan state
     *
     * WHY: This manages the loan lifecycle.
     *
     * PROCESS:
     * 1. Validate the current state
     * 2. Check if transition is allowed
     * 3. Perform any pre-transition checks
     * 4. Update the state
     * 5. Set appropriate date fields
     * 6. Save the loan
     *
     * In a real bank, this would:
     * - Trigger workflows
     * - Send notifications
     * - Update reports
     * - Log to audit systems
     */
    @Transactional
    public LoanResponseDto transitionState(String loanNumber, String targetStateCode, String reason) {
        log.info("Transitioning loan {} from current state to: {}", loanNumber, targetStateCode);

        LoanApplication loan = loanRepository.findByLoanNumber(loanNumber)
                .orElseThrow(() -> new LoanNotFoundException("Loan not found: " + loanNumber));

        String currentStateCode = loan.getStatus();
        LoanState currentState = LoanState.fromCode(currentStateCode);
        LoanState targetState = LoanState.fromCode(targetStateCode);

        // Validate transition
        if (!currentState.canTransitionTo(targetStateCode)) {
            throw new LoanStateTransitionException(
                    "Cannot transition loan from " + currentStateCode + " to " + targetStateCode
            );
        }

        // Perform pre-transition checks
        switch (targetState) {
            case VERIFIED -> {
                // Ensure customer verification is complete
                // In real bank, we'd check KYC status
                log.debug("Verifying loan: {}", loanNumber);
            }
            case APPROVED -> {
                // Ensure credit checks are complete
                // In real bank, we'd check credit score
                loan.setApprovedDate(LocalDateTime.now());
                log.debug("Approving loan: {}", loanNumber);
            }
            case ACTIVE -> {
                // Ensure disbursement is complete
                loan.setDisbursementDate(LocalDateTime.now());
                log.debug("Activating loan: {}", loanNumber);
            }
            case CLOSED -> {
                // Ensure full repayment is complete
                loan.setClosureDate(LocalDateTime.now());
                log.debug("Closing loan: {}", loanNumber);
            }
            default -> {
                // No special handling needed
            }
        }

        // Update state
        loan.setStatus(targetStateCode);
        loan.setUpdatedBy("SYSTEM");

        LoanApplication updatedLoan = loanRepository.save(loan);
        log.info("Loan {} transitioned: {} -> {}", loanNumber, currentStateCode, targetStateCode);

        eventPublisher.publishLoanStatusChangedEvent(loanNumber, currentStateCode, targetStateCode);

        if ("ACTIVE".equals(targetStateCode)) {
            eventPublisher.publishLoanDisbursedEvent(loanNumber);
        }

        return loanMapper.toResponseDto(updatedLoan);
    }

    /**
     * Record a repayment
     *
     * WHY: When a customer makes a payment, we need to:
     * 1. Record the payment
     * 2. Update the loan schedule
     * 3. Recalculate the loan status
     *
     * In a real bank, this would:
     * - Integrate with payment systems
     * - Handle payment allocation (interest first, then principal)
     * - Manage late fees and penalties
     * - Update credit bureau reports
     * - Trigger collections if overdue
     */
    @Transactional
    public LoanRepaymentDto recordRepayment(String loanNumber, LoanRepaymentRequestDto requestDto) {
        log.info("Recording repayment for loan: {}", loanNumber);

        LoanApplication loan = loanRepository.findByLoanNumber(loanNumber)
                .orElseThrow(() -> new LoanNotFoundException("Loan not found: " + loanNumber));

        // Check loan is active
        if (!"ACTIVE".equals(loan.getStatus())) {
            throw new InvalidLoanOperationException(
                    "Loan is not active (status: " + loan.getStatus() + ")"
            );
        }

        // Allocate payment to principal and interest
        // In a real bank, this would be more complex
        BigDecimal paymentAmount = requestDto.getAmount();
        BigDecimal interestDue = calculateInterestDue(loan);
        BigDecimal principalDue = paymentAmount.subtract(interestDue);

        // Create repayment record
        LoanRepayment repayment = LoanRepayment.builder()
                .loan(loan)
                .repaymentReference("REP-" + UUID.randomUUID().toString().substring(0, 8))
                .amount(paymentAmount)
                .principalAmount(principalDue.max(BigDecimal.ZERO))
                .interestAmount(interestDue)
                .repaymentDate(LocalDateTime.now())
                .paymentMethod(requestDto.getPaymentMethod())
                .status("COMPLETED")
                .notes(requestDto.getNotes())
                .createdBy("SYSTEM")
                .updatedBy("SYSTEM")
                .build();

        LoanRepayment savedRepayment = repaymentRepository.save(repayment);

        // Update schedule for this installment
        // In a real bank, we'd match the payment to specific installments
        updateScheduleForRepayment(loan, paymentAmount);

        // Check if loan is fully paid
        if (isFullyPaid(loan)) {
            transitionState(loanNumber, LoanState.CLOSED.getCode(), "Loan fully repaid");
        }

        eventPublisher.publishLoanRepaidEvent(loanNumber, paymentAmount);

        log.info("Repayment recorded for loan: {}, amount: {}", loanNumber, paymentAmount);
        return loanMapper.toRepaymentDto(savedRepayment);
    }

    /**
     * Calculate interest due
     *
     * WHY: Interest is calculated on the outstanding balance.
     *
     * In a real bank, this would use:
     * - Daily interest calculation
     * - Compound interest
     * - Different interest calculation methods
     * - Pre-computed vs simple interest
     */
    private BigDecimal calculateInterestDue(LoanApplication loan) {
        // Simplified: Get the next due installment
        // In a real bank, this would be more complex
        List<LoanSchedule> schedules = scheduleRepository
                .findByLoanAndStatus(loan, "PENDING");

        if (schedules.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // Return the interest for the first pending installment
        return schedules.get(0).getInterestAmount();
    }

    /**
     * Update schedule for repayment
     */
    private void updateScheduleForRepayment(LoanApplication loan, BigDecimal amount) {
        // Find the first pending schedule
        List<LoanSchedule> schedules = scheduleRepository
                .findByLoanAndStatusOrderByInstallmentNumberAsc(loan, "PENDING");

        if (schedules.isEmpty()) {
            return;
        }

        // In a real bank, we'd allocate the payment across multiple installments
        // For simplicity, we pay the first pending installment
        LoanSchedule schedule = schedules.get(0);
        schedule.setStatus("PAID");
        schedule.setPaidDate(LocalDate.now());
        scheduleRepository.save(schedule);
    }

    /**
     * Check if loan is fully paid
     */
    private boolean isFullyPaid(LoanApplication loan) {
        long pendingCount = scheduleRepository
                .countByLoanAndStatus(loan, "PENDING");
        return pendingCount == 0;
    }

    /**
     * Get loan by number
     */
    public LoanResponseDto getLoanByNumber(String loanNumber) {
        LoanApplication loan = loanRepository.findByLoanNumber(loanNumber)
                .orElseThrow(() -> new LoanNotFoundException("Loan not found: " + loanNumber));
        return loanMapper.toResponseDto(loan);
    }

    /**
     * Get loans by customer
     */
    public List<LoanResponseDto> getLoansByCustomer(String customerNumber) {
        List<LoanApplication> loans = loanRepository.findByCustomerNumber(customerNumber);
        return loanMapper.toResponseDtoList(loans);
    }

    /**
     * Get loans by customer with pagination
     */
    public Page<LoanResponseDto> getLoansByCustomer(String customerNumber, Pageable pageable) {
        Page<LoanApplication> loans = loanRepository.findByCustomerNumber(customerNumber, pageable);
        return loans.map(loanMapper::toResponseDto);
    }

    /**
     * Get all loans with pagination
     */
    public Page<LoanResponseDto> getAllLoans(Pageable pageable) {
        Page<LoanApplication> loans = loanRepository.findAll(pageable);
        return loans.map(loanMapper::toResponseDto);
    }

    /**
     * Get loans by status
     */
    public List<LoanResponseDto> getLoansByStatus(String status) {
        List<LoanApplication> loans = loanRepository.findByStatus(status);
        return loanMapper.toResponseDtoList(loans);
    }

    /**
     * Get loan schedule
     */
    public List<LoanScheduleDto> getLoanSchedule(String loanNumber) {
        LoanApplication loan = loanRepository.findByLoanNumber(loanNumber)
                .orElseThrow(() -> new LoanNotFoundException("Loan not found: " + loanNumber));
        return loanMapper.toScheduleDtoList(loan.getSchedules());
    }

    /**
     * Get loan repayments
     */
    public List<LoanRepaymentDto> getLoanRepayments(String loanNumber) {
        LoanApplication loan = loanRepository.findByLoanNumber(loanNumber)
                .orElseThrow(() -> new LoanNotFoundException("Loan not found: " + loanNumber));
        return loanMapper.toRepaymentDtoList(loan.getRepayments());
    }
    
}
