package com.belajar.springboot.dp.dto;

import java.math.BigDecimal;

public interface TransaksiSummaryProjection {

    Integer getTahun();

    Integer getBulan();

    Long getTotalCount();

    BigDecimal getTotalAmount();

    BigDecimal getTotalDebit();

    BigDecimal getTotalKredit();
}
