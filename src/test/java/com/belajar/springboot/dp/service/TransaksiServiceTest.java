package com.belajar.springboot.dp.service;

import com.belajar.springboot.dp.dto.TransaksiSummaryProjection;
import com.belajar.springboot.dp.dto.TransaksiSummaryResponse;
import com.belajar.springboot.dp.repository.TransaksiRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransaksiServiceTest {

    @Mock
    private TransaksiRepository transaksiRepository;

    @InjectMocks
    private TransaksiService transaksiService;

    @Test
    @DisplayName("Should return monthly summary with formatted month names (e.g. Feb 2025, Mar 2025)")
    void getMonthlySummary_Success() {
        // Arrange
        TransaksiSummaryProjection projFeb = new TransaksiSummaryProjection() {
            @Override public Integer getTahun() { return 2025; }
            @Override public Integer getBulan() { return 2; }
            @Override public Long getTotalCount() { return 3L; }
            @Override public BigDecimal getTotalAmount() { return new BigDecimal("1150000.00"); }
            @Override public BigDecimal getTotalDebit() { return new BigDecimal("650000.00"); }
            @Override public BigDecimal getTotalKredit() { return new BigDecimal("500000.00"); }
        };

        TransaksiSummaryProjection projMar = new TransaksiSummaryProjection() {
            @Override public Integer getTahun() { return 2025; }
            @Override public Integer getBulan() { return 3; }
            @Override public Long getTotalCount() { return 2L; }
            @Override public BigDecimal getTotalAmount() { return new BigDecimal("7750000.00"); }
            @Override public BigDecimal getTotalDebit() { return new BigDecimal("250000.00"); }
            @Override public BigDecimal getTotalKredit() { return new BigDecimal("7500000.00"); }
        };

        when(transaksiRepository.getMonthlySummary(2025, null))
                .thenReturn(List.of(projFeb, projMar));

        // Act
        List<TransaksiSummaryResponse> result = transaksiService.getMonthlySummary(2025, null);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        // Check Feb 2025
        TransaksiSummaryResponse feb = result.get(0);
        assertEquals(2025, feb.getTahun());
        assertEquals(2, feb.getBulan());
        assertEquals("Feb 2025", feb.getPeriode());
        assertEquals(3L, feb.getTotalTransaksi());
        assertEquals(new BigDecimal("1150000.00"), feb.getTotalNominal());
        assertEquals(new BigDecimal("650000.00"), feb.getTotalDebit());
        assertEquals(new BigDecimal("500000.00"), feb.getTotalKredit());

        // Check Mar 2025
        TransaksiSummaryResponse mar = result.get(1);
        assertEquals(2025, mar.getTahun());
        assertEquals(3, mar.getBulan());
        assertEquals("Mar 2025", mar.getPeriode());

        verify(transaksiRepository, times(1)).getMonthlySummary(2025, null);
    }
}
