package com.financeapp.service;

import com.financeapp.dto.FinancialRecordRequest;
import com.financeapp.dto.FinancialRecordResponse;
import com.financeapp.entity.TransactionType;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;

import org.springframework.data.domain.Page;


public interface FinancialRecordService {

    FinancialRecordResponse createRecord(FinancialRecordRequest request);

    FinancialRecordResponse updateRecord(Long id, FinancialRecordRequest request);

    void deleteRecord(Long id);

    Page<FinancialRecordResponse> getAllRecords(Pageable pageable);

    Page<FinancialRecordResponse> filterRecords(
            LocalDate startDate,
            LocalDate endDate,
            String category,
            TransactionType type,
            Pageable pageable
    );

    FinancialRecordResponse getRecordById(Long id);
}