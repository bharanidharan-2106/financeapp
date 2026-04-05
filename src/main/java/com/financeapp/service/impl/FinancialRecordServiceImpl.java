package com.financeapp.service.impl;

import com.financeapp.dto.BulkRecordRequest;
import com.financeapp.dto.FinancialRecordRequest;
import com.financeapp.dto.FinancialRecordResponse;
import com.financeapp.entity.FinancialRecord;
import com.financeapp.entity.TransactionType;
import com.financeapp.entity.User;
import com.financeapp.exception.ResourceNotFoundException;
import com.financeapp.repository.FinancialRecordRepository;
import com.financeapp.repository.UserRepository;
import com.financeapp.security.CustomUserDetails;
import com.financeapp.service.FinancialRecordService;
import com.financeapp.util.FinancialRecordMapper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class FinancialRecordServiceImpl implements FinancialRecordService {

    private final FinancialRecordRepository financialRecordRepository;
    private final UserRepository userRepository;

    public FinancialRecordServiceImpl(FinancialRecordRepository financialRecordRepository,
                                      UserRepository userRepository) {
        this.financialRecordRepository = financialRecordRepository;
        this.userRepository = userRepository;
    }

    @Override
    public FinancialRecordResponse createRecord(FinancialRecordRequest request) {
        User authenticatedUser = resolveAuthenticatedUser();
        FinancialRecord record = FinancialRecordMapper.toEntity(request, authenticatedUser);
        FinancialRecord saved = financialRecordRepository.save(record);
        return FinancialRecordMapper.toResponse(saved);
    }

    @Override
    public Page<FinancialRecordResponse> createBulkRecords(BulkRecordRequest request) {
        User authenticatedUser = resolveAuthenticatedUser();
        
        var records = request.getRecords().stream()
                .map(recordRequest -> {
                    FinancialRecord record = FinancialRecordMapper.toEntity(recordRequest, authenticatedUser);
                    return financialRecordRepository.save(record);
                })
                .collect(Collectors.toList());
        
        // Convert the list to a Page and return
        List<FinancialRecordResponse> responses = records.stream()
                .map(FinancialRecordMapper::toResponse)
                .collect(Collectors.toList());
        
        return new org.springframework.data.domain.PageImpl<>(responses);
    }

    @Override
    public FinancialRecordResponse updateRecord(Long id, FinancialRecordRequest request) {
        FinancialRecord existing = findRecordOrThrow(id);

        existing.setAmount(request.getAmount());
        existing.setType(request.getType());
        existing.setCategory(request.getCategory());
        existing.setDate(request.getDate());
        existing.setDescription(request.getDescription());

        FinancialRecord updated = financialRecordRepository.save(existing);
        return FinancialRecordMapper.toResponse(updated);
    }

    @Override
    public void deleteRecord(Long id) {
        FinancialRecord existing = findRecordOrThrow(id);
        User authenticatedUser = resolveAuthenticatedUser();
        
        existing.setIsDeleted(true);
        existing.setDeletedAt(LocalDateTime.now());
        existing.setDeletedBy(authenticatedUser);
        
        financialRecordRepository.save(existing);
    }

    @Override
    public void restoreRecord(Long id) {
        FinancialRecord existing = financialRecordRepository.findByIdIncludingDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Financial record", id));
        
        if (!existing.getIsDeleted()) {
            throw new IllegalStateException("Record is not deleted and cannot be restored");
        }
        
        existing.setIsDeleted(false);
        existing.setDeletedAt(null);
        existing.setDeletedBy(null);
        
        financialRecordRepository.save(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FinancialRecordResponse> getDeletedRecords(Pageable pageable) {
        return financialRecordRepository.findDeletedRecords(pageable)
                .map(FinancialRecordMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FinancialRecordResponse> getAllRecords(Pageable pageable) {
        return financialRecordRepository.findAll(pageable)
                .map(FinancialRecordMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public FinancialRecordResponse getRecordById(Long id) {
        return FinancialRecordMapper.toResponse(findRecordOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FinancialRecordResponse> filterRecords(
            LocalDate startDate,
            LocalDate endDate,
            String category,
            TransactionType type,
            Pageable pageable) {

        return financialRecordRepository
                .filterRecords(startDate, endDate, category, type, pageable)
                .map(FinancialRecordMapper::toResponse);
    }

    private FinancialRecord findRecordOrThrow(Long id) {
        return financialRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Financial record", id));
    }

    private User resolveAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Authenticated user not found in database: " + email));
    }
}