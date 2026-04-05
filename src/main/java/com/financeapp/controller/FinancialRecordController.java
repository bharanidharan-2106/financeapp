package com.financeapp.controller;

import com.financeapp.dto.BulkRecordRequest;
import com.financeapp.dto.FinancialRecordRequest;
import com.financeapp.dto.FinancialRecordResponse;
import com.financeapp.entity.TransactionType;
import com.financeapp.service.FinancialRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import java.time.LocalDate;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/records")
@Tag(name = "Financial Records", description = "APIs for managing financial records with role-based access control")
public class FinancialRecordController {

    private final FinancialRecordService financialRecordService;

    public FinancialRecordController(FinancialRecordService financialRecordService) {
        this.financialRecordService = financialRecordService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Create a financial record",
            description = "Creates a new financial record. The record is associated with the authenticated user. Access: ADMIN only."
    )
    public ResponseEntity<FinancialRecordResponse> createRecord(
            @Valid @RequestBody FinancialRecordRequest request) {

        FinancialRecordResponse response = financialRecordService.createRecord(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Create multiple financial records",
            description = "Creates multiple financial records in a single request. All records are associated with the authenticated user. Access: ADMIN only."
    )
    public ResponseEntity<Page<FinancialRecordResponse>> createBulkRecords(
            @Valid @RequestBody BulkRecordRequest request) {

        Page<FinancialRecordResponse> response = financialRecordService.createBulkRecords(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST', 'VIEWER')")
    @Operation(
            summary = "Get all financial records",
            description = "Returns all financial records ordered by insertion. Sorting is based on Date in Descending order. Access: ADMIN, ANALYST, VIEWER."
    )
    public ResponseEntity<Page<FinancialRecordResponse>> getAllRecords(
            @PageableDefault(size = 5, sort = "date", direction = Sort.Direction.DESC)
            Pageable pageable) {

        return ResponseEntity.ok(financialRecordService.getAllRecords(pageable));
    }

    @GetMapping("/filter")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    @Operation(
            summary = "Filter financial records",
            description = "Filters records by optional date range, category, and/or transaction type. All parameters are optional. Sorting is based on Date in Descending order. Access: ADMIN, ANALYST."
    )
    public ResponseEntity<Page<FinancialRecordResponse>> filterRecords(

            @Parameter(description = "Start date (inclusive), format: yyyy-mm-dd")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @Parameter(description = "End date (inclusive), format: yyyy-mm-dd")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,

            @Parameter(description = "Category name (case-insensitive match)")
            @RequestParam(required = false)
            String category,

            @Parameter(description = "Transaction type: INCOME or EXPENSE")
            @RequestParam(required = false)
            TransactionType type,
    		
    		@PageableDefault(size = 5, sort = "date", direction = Sort.Direction.DESC)
    		Pageable pageable) {
    
        	return ResponseEntity.ok(
                financialRecordService.filterRecords(startDate, endDate, category, type, pageable)
        );
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get soft-deleted financial records",
            description = "Returns all soft-deleted financial records. Access: ADMIN only."
    )
    public ResponseEntity<Page<FinancialRecordResponse>> getDeletedRecords(
            @PageableDefault(size = 5, sort = "date", direction = Sort.Direction.DESC)
            Pageable pageable) {

        return ResponseEntity.ok(financialRecordService.getDeletedRecords(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST', 'VIEWER')")
    @Operation(
            summary = "Get a financial record by ID",
            description = "Returns a single financial record by its ID. Access: ADMIN, ANALYST, VIEWER."
    )
    public ResponseEntity<FinancialRecordResponse> getRecordById(
            @Parameter(description = "Financial record ID") @PathVariable Long id) {

        return ResponseEntity.ok(financialRecordService.getRecordById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update a financial record",
            description = "Fully updates an existing financial record by ID. Access: ADMIN only."
    )
    public ResponseEntity<FinancialRecordResponse> updateRecord(
            @Parameter(description = "Financial record ID") @PathVariable Long id,
            @Valid @RequestBody FinancialRecordRequest request) {

        return ResponseEntity.ok(financialRecordService.updateRecord(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Soft delete a financial record",
            description = "Soft deletes a financial record (data is preserved). Access: ADMIN only."
    )
    public ResponseEntity<String> deleteRecord(
            @Parameter(description = "Financial record ID") @PathVariable Long id) {

        financialRecordService.deleteRecord(id);
        return ResponseEntity.ok("Record soft-deleted successfully");
    }

    @PatchMapping("/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Restore a soft-deleted financial record",
            description = "Restores a previously soft-deleted financial record. Access: ADMIN only."
    )
    public ResponseEntity<FinancialRecordResponse> restoreRecord(
            @Parameter(description = "Financial record ID") @PathVariable Long id) {

        financialRecordService.restoreRecord(id);
        return ResponseEntity.ok(financialRecordService.getRecordById(id));
    }
}