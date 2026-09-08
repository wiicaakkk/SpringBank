package com.belajar.springboot.at.repository;

import com.belajar.springboot.at.entity.AutoTransferHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AutoTransferHistoryRepository extends JpaRepository<AutoTransferHistory, Long> {

    List<AutoTransferHistory> findByKodeInstruksiOrderByTanggalEksekusiDesc(String kodeInstruksi);
}