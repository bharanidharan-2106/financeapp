package com.financeapp.dto;

import com.financeapp.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialRecordResponse {

    private Long id;
    private Double amount;
    private TransactionType type;
    private String category;
    private LocalDate date;
    private String description;
    private String createdBy;
    private LocalDateTime createdAt;
}