package com.belajar.springboot.rc.entity;

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
@Table(name = "nasabah")
public class Nasabah {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String cif;

    @Column(unique = true, nullable = false, length = 16)
    private String nik;

    @Column(nullable = false, length = 100)
    private String namaLengkap;

    @Column(nullable = false, length = 50)
    private String tempatLahir;

    @Column(nullable = false)
    private LocalDate tanggalLahir;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private JenisKelamin jenisKelamin;

    @Column(nullable = false, length = 100)
    private String ibuKandung;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String alamat;

    @Column(nullable = false, length = 15)
    private String nomorHp;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 50)
    private String pekerjaan;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal penghasilanBulanan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private StatusNasabah statusNasabah;

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
