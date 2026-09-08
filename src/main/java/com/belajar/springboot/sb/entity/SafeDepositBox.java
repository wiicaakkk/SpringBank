package com.belajar.springboot.sb.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "safe_deposit_box", uniqueConstraints = {
    @UniqueConstraint(name = "uk_box_nomor", columnNames = "nomor_box")
})
public class SafeDepositBox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nomor_box", nullable = false, length = 10)
    private String nomorBox;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private StatusBox status;

    @Column(name = "cif_penyewa", length = 20)
    private String cifPenyewa;

    @Column(name = "nama_penyewa", length = 100)
    private String namaPenyewa;

    @Column(name = "nomor_rekening", length = 20)
    private String nomorRekening;

    @Column(name = "tanggal_mulai")
    private LocalDate tanggalMulai;

    @Column(name = "tanggal_jatuh_tempo")
    private LocalDate tanggalJatuhTempo;

    @Column(name = "tarif_sewa", precision = 19, scale = 2)
    private BigDecimal tarifSewa;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = StatusBox.TERSEDIA;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}