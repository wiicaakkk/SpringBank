package com.belajar.springboot.entity;

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

    @Column(nullable = false, unique = true, length = 20)
    private String cif; // Customer Information File Number e.g. CIF202607260001

    @Column(nullable = false, unique = true, length = 16)
    private String nik; // Nomor Induk Kependudukan (KTP)

    @Column(nullable = false)
    private String namaLengkap;

    private String tempatLahir;
    
    private LocalDate tanggalLahir;

    @Enumerated(EnumType.STRING)
    private JenisKelamin jenisKelamin;

    @Column(nullable = false)
    private String ibuKandung; // Mandatory banking security field

    @Column(columnDefinition = "TEXT")
    private String alamat;

    @Column(nullable = false, length = 20)
    private String nomorHp;

    @Column(nullable = false, unique = true)
    private String email;

    private String pekerjaan;

    private BigDecimal penghasilanBulanan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusNasabah statusNasabah;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.statusNasabah == null) {
            this.statusNasabah = StatusNasabah.AKTIF;
        }
    }
}
