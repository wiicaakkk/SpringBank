package com.belajar.springboot.gl.repository;

import com.belajar.springboot.gl.entity.GlEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface GlEntryRepository extends JpaRepository<GlEntry, Long> {

    @Query("select g.kodeAkun, " +
            "sum(case when g.sisi = com.belajar.springboot.gl.entity.SisiPembukuan.DEBIT then g.nominal else 0 end), " +
            "sum(case when g.sisi = com.belajar.springboot.gl.entity.SisiPembukuan.KREDIT then g.nominal else 0 end) " +
            "from GlEntry g where g.tanggalPembukuan <= :tanggal group by g.kodeAkun")
    List<Object[]> sumSisiPerAkunUntil(@Param("tanggal") LocalDate tanggal);

    @Query("select g.kodeAkun, " +
            "sum(case when g.sisi = com.belajar.springboot.gl.entity.SisiPembukuan.DEBIT then g.nominal else 0 end), " +
            "sum(case when g.sisi = com.belajar.springboot.gl.entity.SisiPembukuan.KREDIT then g.nominal else 0 end) " +
            "from GlEntry g where g.tanggalPembukuan < :tanggal group by g.kodeAkun")
    List<Object[]> sumSisiPerAkunBefore(@Param("tanggal") LocalDate tanggal);

    List<GlEntry> findByKodeAkunAndTanggalPembukuanBetweenOrderByTanggalPembukuanAscIdAsc(
            String kodeAkun, LocalDate dari, LocalDate sampai);

    List<GlEntry> findByTanggalPembukuanBetweenOrderByTanggalPembukuanAscIdAsc(
            LocalDate dari, LocalDate sampai);
}