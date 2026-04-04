package com.financeapp.service;

import com.financeapp.dto.DashboardSummaryResponse;
import com.financeapp.repository.FinancialRecordRepository;
import com.financeapp.service.impl.DashboardServiceImpl;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private FinancialRecordRepository repository;

    @InjectMocks
    private DashboardServiceImpl service;

    @Test
    void shouldReturnSummaryCorrectly() {

        when(repository.getTotalIncome()).thenReturn(10000.0);
        when(repository.getTotalExpense()).thenReturn(4000.0);

        DashboardSummaryResponse response = service.getSummary();

        assertEquals(10000.0, response.getTotalIncome());
        assertEquals(4000.0, response.getTotalExpense());
        assertEquals(6000.0, response.getNetBalance());
    }
}