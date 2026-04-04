package com.financeapp.service;

import com.financeapp.dto.FinancialRecordRequest;
import com.financeapp.dto.FinancialRecordResponse;
import com.financeapp.entity.FinancialRecord;
import com.financeapp.entity.Role;
import com.financeapp.entity.Status;
import com.financeapp.entity.TransactionType;
import com.financeapp.entity.User;
import com.financeapp.exception.ResourceNotFoundException;
import com.financeapp.repository.FinancialRecordRepository;
import com.financeapp.repository.UserRepository;
import com.financeapp.security.CustomUserDetails;
import com.financeapp.service.impl.FinancialRecordServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FinancialRecordServiceTest {

    @Mock
    private FinancialRecordRepository financialRecordRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FinancialRecordServiceImpl financialRecordService;

    private User testUser;
    private FinancialRecord testRecord;
    private FinancialRecordRequest testRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@gmail.com")
                .password("encoded")
                .role(Role.ADMIN)
                .status(Status.ACTIVE)
                .build();

        testRecord = FinancialRecord.builder()
                .id(1L)
                .amount(5000.0)
                .type(TransactionType.EXPENSE)
                .category("Food")
                .date(LocalDate.now())
                .description("Groceries")
                .createdBy(testUser)
                .build();

        testRequest = new FinancialRecordRequest();
        testRequest.setAmount(5000.0);
        testRequest.setType(TransactionType.EXPENSE);
        testRequest.setCategory("Food");
        testRequest.setDate(LocalDate.now());
        testRequest.setDescription("Groceries");
    }

    @Test
    void shouldCreateRecord() {
        mockSecurityContext();

        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(testUser));
        when(financialRecordRepository.save(any(FinancialRecord.class))).thenReturn(testRecord);

        FinancialRecordResponse response = financialRecordService.createRecord(testRequest);

        assertNotNull(response);
        assertEquals(5000.0, response.getAmount());
        assertEquals(TransactionType.EXPENSE, response.getType());
        assertEquals("Food", response.getCategory());
        verify(financialRecordRepository, times(1)).save(any(FinancialRecord.class));
    }

    @Test
    void shouldUpdateRecord() {
        Long recordId = 1L;
        FinancialRecordRequest updateRequest = new FinancialRecordRequest();
        updateRequest.setAmount(6000.0);
        updateRequest.setType(TransactionType.INCOME);
        updateRequest.setCategory("Salary");
        updateRequest.setDate(LocalDate.now());
        updateRequest.setDescription("Monthly salary");

        FinancialRecord updatedRecord = FinancialRecord.builder()
                .id(recordId)
                .amount(6000.0)
                .type(TransactionType.INCOME)
                .category("Salary")
                .date(LocalDate.now())
                .description("Monthly salary")
                .createdBy(testUser)
                .build();

        when(financialRecordRepository.findById(recordId)).thenReturn(Optional.of(testRecord));
        when(financialRecordRepository.save(any(FinancialRecord.class))).thenReturn(updatedRecord);

        FinancialRecordResponse response = financialRecordService.updateRecord(recordId, updateRequest);

        assertNotNull(response);
        assertEquals(6000.0, response.getAmount());
        assertEquals(TransactionType.INCOME, response.getType());
        assertEquals("Salary", response.getCategory());
        verify(financialRecordRepository, times(1)).save(any(FinancialRecord.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentRecord() {
        Long recordId = 999L;

        when(financialRecordRepository.findById(recordId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            financialRecordService.updateRecord(recordId, testRequest);
        });
    }

    @Test
    void shouldDeleteRecord() {
        Long recordId = 1L;

        when(financialRecordRepository.findById(recordId)).thenReturn(Optional.of(testRecord));

        financialRecordService.deleteRecord(recordId);

        verify(financialRecordRepository, times(1)).delete(testRecord);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentRecord() {
        Long recordId = 999L;

        when(financialRecordRepository.findById(recordId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            financialRecordService.deleteRecord(recordId);
        });
    }

    @Test
    void shouldGetAllRecords() {
        Pageable pageable = PageRequest.of(0, 10);
        FinancialRecord record2 = FinancialRecord.builder()
                .id(2L)
                .amount(3000.0)
                .type(TransactionType.INCOME)
                .category("Bonus")
                .date(LocalDate.now())
                .description("Performance bonus")
                .createdBy(testUser)
                .build();

        List<FinancialRecord> records = Arrays.asList(testRecord, record2);
        Page<FinancialRecord> page = new PageImpl<>(records, pageable, records.size());

        when(financialRecordRepository.findAll(pageable)).thenReturn(page);

        Page<FinancialRecordResponse> response = financialRecordService.getAllRecords(pageable);

        assertNotNull(response);
        assertEquals(2, response.getContent().size());
        verify(financialRecordRepository, times(1)).findAll(pageable);
    }

    @Test
    void shouldGetRecordById() {
        Long recordId = 1L;

        when(financialRecordRepository.findById(recordId)).thenReturn(Optional.of(testRecord));

        FinancialRecordResponse response = financialRecordService.getRecordById(recordId);

        assertNotNull(response);
        assertEquals(5000.0, response.getAmount());
        assertEquals("Food", response.getCategory());
        verify(financialRecordRepository, times(1)).findById(recordId);
    }

    @Test
    void shouldThrowExceptionWhenGettingNonExistentRecord() {
        Long recordId = 999L;

        when(financialRecordRepository.findById(recordId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            financialRecordService.getRecordById(recordId);
        });
    }

    @Test
    void shouldFilterRecords() {
        LocalDate startDate = LocalDate.now().minusMonths(1);
        LocalDate endDate = LocalDate.now();
        String category = "Food";
        TransactionType type = TransactionType.EXPENSE;
        Pageable pageable = PageRequest.of(0, 10);

        List<FinancialRecord> filteredRecords = Arrays.asList(testRecord);
        Page<FinancialRecord> page = new PageImpl<>(filteredRecords, pageable, filteredRecords.size());

        when(financialRecordRepository.filterRecords(startDate, endDate, category, type, pageable))
                .thenReturn(page);

        Page<FinancialRecordResponse> response = financialRecordService.filterRecords(
                startDate, endDate, category, type, pageable);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        verify(financialRecordRepository, times(1))
                .filterRecords(startDate, endDate, category, type, pageable);
    }

    @Test
    void shouldFilterRecordsWithoutType() {
        LocalDate startDate = LocalDate.now().minusMonths(1);
        LocalDate endDate = LocalDate.now();
        String category = "Food";
        Pageable pageable = PageRequest.of(0, 10);

        List<FinancialRecord> filteredRecords = Arrays.asList(testRecord);
        Page<FinancialRecord> page = new PageImpl<>(filteredRecords, pageable, filteredRecords.size());

        when(financialRecordRepository.filterRecords(startDate, endDate, category, null, pageable))
                .thenReturn(page);

        Page<FinancialRecordResponse> response = financialRecordService.filterRecords(
                startDate, endDate, category, null, pageable);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
    }

    private void mockSecurityContext() {
        CustomUserDetails userDetails = new CustomUserDetails(testUser);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }
}
