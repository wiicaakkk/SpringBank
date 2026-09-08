package com.belajar.springboot.gl.entity;

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
@Table(name = "gl_entry", indexes = {
    @Index(name = "idx_glentry_tanggal", columnList = "tanggal_pembukuan"),
    @Index(name = "idx_glentry_akun", columnList = "kode_akun"),
    @Index(name = "idx_glentry_ref", columnList = "ref_transaksi")
})
public class GlEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "kode_akun", nullable = false, length = 12)
    private String kodeAkun;

    @Enumerated(EnumType.STRING)
    @Column(name = "sisi", nullable = false, length = 10)
    private SisiPembukuan sisi;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal nominal;

    @Column(name = "tanggal_pembukuan", nullable = false)
    private LocalDate tanggalPembukuan;

    @Column(name = "ref_transaksi", length = 40)
    private String refTransaksi;

    @Column(length = 255)
    private String keterangan;

    @Column(name = "operator_id", length = 20)
    private String operatorId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (tanggalPembukuan == null) {
            tanggalPembukuan = LocalDate.now();
        }
    }
}