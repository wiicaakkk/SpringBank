package com.belajar.springboot.at.repository;

import com.belajar.springboot.at.entity.AtStatusInstruksi;
import com.belajar.springboot.at.entity.AutoTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AutoTransferRepository extends JpaRepository<AutoTransfer, Long> {

    Optional<AutoTransfer> findByKodeInstruksi(String kodeInstruksi);

    boolean existsByKodeInstruksi(String kodeInstruksi);

    List<AutoTransfer> findByStatusAtOrderByCreatedAtDesc(AtStatusInstruksi statusAt);

    List<AutoTransfer> findAllByOrderByCreatedAtDesc();

    List<AutoTransfer> findByStatusAtAndTanggalBerikutnyaLessThanEqual(AtStatusInstruksi statusAt, LocalDate tanggal);

    long countByKodeInstruksiStartingWith(String prefix);
}