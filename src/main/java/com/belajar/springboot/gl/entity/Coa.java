package com.belajar.springboot.gl.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "coa", indexes = {
    @Index(name = "idx_coa_kode", columnList = "kode_akun", unique = true)
})
public class Coa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "kode_akun", nullable = false, length = 12)
    private String kodeAkun;

    @Column(name = "nama_akun", nullable = false, length = 80)
    private String namaAkun;

    @Enumerated(EnumType.STRING)
    @Column(name = "jenis_akun", nullable = false, length = 20)
    private JenisAkun jenisAkun;

    @Enumerated(EnumType.STRING)
    @Column(name = "posisi_normal", nullable = false, length = 10)
    private PosisiNormal posisiNormal;

    @Builder.Default
    @Column(nullable = false)
    private Boolean aktif = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (aktif == null) {
            aktif = true;
        }
    }
}