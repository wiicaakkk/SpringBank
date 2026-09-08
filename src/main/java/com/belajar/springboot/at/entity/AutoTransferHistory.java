package com.belajar.springboot.at.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "auto_transfer_history", indexes = {
    @Index(name = "idx_ath_kode", columnList = "kode_instruksi")
})
public class AutoTransferHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "kode_instruksi", nullable = false, length = 30)
    private String kodeInstruksi;

    @Column(name = "nomor_rekening_debit", nullable = false, length = 20)
    private String nomorRekeningDebit;

    @Column(name = "nomor_rekening_kredit", nullable = false, length = 20)
    private String nomorRekeningKredit;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal nominal;

    @Column(name = "tanggal_eksekusi", nullable = false)
    private LocalDateTime tanggalEksekusi;

    @Column(name = "status_eksekusi", nullable = false, length = 20)
    private String statusEksekusi;

    @Column(length = 255)
    private String keterangan;

    @Column(name = "operator_eksekusi", nullable = false, length = 30)
    private String operatorEksekusi;
}