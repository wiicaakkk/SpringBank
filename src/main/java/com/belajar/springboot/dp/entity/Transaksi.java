package com.belajar.springboot.dp.entity;

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
@Table(name = "transaksi", indexes = {
    @Index(name = "idx_transaksi_created_at", columnList = "created_at"),
    @Index(name = "idx_transaksi_norek", columnList = "nomor_rekening"),
    @Index(name = "idx_transaksi_norek_created", columnList = "nomor_rekening, created_at")
})
public class Transaksi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nomor_rekening", nullable = false, length = 20)
    private String nomorRekening;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipe_transaksi", nullable = false, length = 30)
    private TipeTransaksi tipeTransaksi;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(length = 255)
    private String deskripsi;

    @Column(length = 50)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "SUCCESS";
        }
    }
}
