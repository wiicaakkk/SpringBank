package com.belajar.springboot.dp.repository;

import com.belajar.springboot.dp.dto.TransaksiSummaryProjection;
import com.belajar.springboot.dp.entity.Transaksi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransaksiRepository extends JpaRepository<Transaksi, Long> {

    List<Transaksi> findByNomorRekeningOrderByCreatedAtDesc(String nomorRekening);

    List<Transaksi> findByNomorRekeningAndCreatedAtBetweenOrderByCreatedAtAsc(
            String nomorRekening, LocalDateTime dari, LocalDateTime sampai);

    List<Transaksi> findByCreatedAtBetweenOrderByCreatedAtAsc(LocalDateTime dari, LocalDateTime sampai);

    @Query("""
        SELECT 
            YEAR(t.createdAt) AS tahun,
            MONTH(t.createdAt) AS bulan,
            COUNT(t.id) AS totalCount,
            COALESCE(SUM(t.amount), 0) AS totalAmount,
            COALESCE(SUM(CASE WHEN t.tipeTransaksi = com.belajar.springboot.dp.entity.TipeTransaksi.DEBIT 
                                  OR t.tipeTransaksi = com.belajar.springboot.dp.entity.TipeTransaksi.TOPUP_PULSA 
                                  OR t.tipeTransaksi = com.belajar.springboot.dp.entity.TipeTransaksi.PAKET_DATA 
                                  OR t.tipeTransaksi = com.belajar.springboot.dp.entity.TipeTransaksi.PAYMENT 
                             THEN t.amount ELSE 0 END), 0) AS totalDebit,
            COALESCE(SUM(CASE WHEN t.tipeTransaksi = com.belajar.springboot.dp.entity.TipeTransaksi.KREDIT 
                             THEN t.amount ELSE 0 END), 0) AS totalKredit
        FROM Transaksi t
        WHERE (:tahun IS NULL OR YEAR(t.createdAt) = :tahun)
          AND (:nomorRekening IS NULL OR t.nomorRekening = :nomorRekening)
        GROUP BY YEAR(t.createdAt), MONTH(t.createdAt)
        ORDER BY YEAR(t.createdAt) ASC, MONTH(t.createdAt) ASC
        """)
    List<TransaksiSummaryProjection> getMonthlySummary(
            @Param("tahun") Integer tahun,
            @Param("nomorRekening") String nomorRekening
    );
}
