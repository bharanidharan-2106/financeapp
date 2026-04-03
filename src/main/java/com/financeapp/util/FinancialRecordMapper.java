package com.financeapp.util;

import com.financeapp.dto.FinancialRecordRequest;
import com.financeapp.dto.FinancialRecordResponse;
import com.financeapp.entity.FinancialRecord;
import com.financeapp.entity.User;

public final class FinancialRecordMapper {

    private FinancialRecordMapper() {}

    public static FinancialRecord toEntity(FinancialRecordRequest request, User createdBy) {
        return FinancialRecord.builder()
                .amount(request.getAmount())
                .type(request.getType())
                .category(request.getCategory())
                .date(request.getDate())
                .description(request.getDescription())
                .createdBy(createdBy)
                .build();
    }

    public static FinancialRecordResponse toResponse(FinancialRecord record) {
        return FinancialRecordResponse.builder()
                .id(record.getId())
                .amount(record.getAmount())
                .type(record.getType())
                .category(record.getCategory())
                .date(record.getDate())
                .description(record.getDescription())
                .createdBy(record.getCreatedBy().getName())
                .createdAt(record.getCreatedAt())
                .build();
    }
}