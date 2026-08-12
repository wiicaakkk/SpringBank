package com.belajar.springboot.repository;

import com.belajar.springboot.entity.NasabahHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NasabahHistoryRepository extends JpaRepository<NasabahHistory, Long> {

    /**
     * Ambil seluruh riwayat perubahan nasabah berdasarkan CIF, diurutkan dari yang terbaru.
     */
    List<NasabahHistory> findByCifOrderByCreatedAtDesc(String cif);

    /**
     * Ambil seluruh audit log nasabah di sistem perbankan.
     */
    List<NasabahHistory> findAllByOrderByCreatedAtDesc();
}
