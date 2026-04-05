package com.financeapp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BulkRecordRequest {

    @NotEmpty(message = "Records list cannot be empty")
    @Valid
    private List<FinancialRecordRequest> records;
}
