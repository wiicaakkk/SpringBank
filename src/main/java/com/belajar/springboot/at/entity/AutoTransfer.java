package com.belajar.springboot.at.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "auto_transfer", indexes = {
    @Index(name = "idx_at_berikutnya", columnList = "tanggal_berikutnya, status_at"),
    @Index(name = "idx_at_kode", columnList = "kode_instruksi")
})
public class AutoTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "kode_instruksi", unique = true, nullable = false, length = 30)
    private String kodeInstruksi;

    @Column(name = "nomor_rekening_debit", nullable = false, length = 20)
    private String nomorRekeningDebit;

    @Column(name = "nomor_rekening_kredit", nullable = false, length = 20)
    private String nomorRekeningKredit;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal nominal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AtPeriode periode;

    @Column(name = "tanggal_mulai", nullable = false)
    private LocalDate tanggalMulai;

    @Column(name = "tanggal_berikutnya", nullable = false)
    private LocalDate tanggalBerikutnya;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_at", nullable = false, length = 20)
    private AtStatusInstruksi statusAt;

    @Column(name = "operator_dibuat", nullable = false, length = 30)
    private String operatorDibuat;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}