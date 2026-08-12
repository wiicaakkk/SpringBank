package com.belajar.springboot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "nasabah_history")
public class NasabahHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String cif;

    @Column(nullable = false, length = 50)
    private String actionType; // REGISTRATION, UPDATE_PROFILE, STATUS_CHANGE

    @Column(nullable = false, length = 500)
    private String keterangan; // Catatan perubahan data

    @Column(nullable = false, length = 50)
    private String updatedBy; // Siapa yang mengubah (misal: SYSTEM, TELLER_01)

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
