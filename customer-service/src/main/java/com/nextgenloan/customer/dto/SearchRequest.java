package com.nextgenloan.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {
    private int page = 0;
    private int size = 20;
    private String sort = "lastName,asc";

    // Search criteria
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private List<String> statuses;
    private LocalDate dateOfBirthFrom;
    private LocalDate dateOfBirthTo;
    private LocalDate createdFrom;
    private LocalDate createdTo;
    private String searchTerm;  // Search across name and email

    // Logical operators (could be extended)
    private Boolean requireAll = true;  // AND vs OR
}