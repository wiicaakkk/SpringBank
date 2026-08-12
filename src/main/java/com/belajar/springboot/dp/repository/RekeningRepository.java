package com.belajar.springboot.dp.repository;

import com.belajar.springboot.dp.entity.Rekening;
import com.belajar.springboot.rc.entity.Nasabah;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RekeningRepository extends JpaRepository<Rekening, Long> {

    Optional<Rekening> findByNomorRekening(String nomorRekening);

    List<Rekening> findByNasabah(Nasabah nasabah);

    List<Rekening> findByNasabahCif(String cif);

    boolean existsByNomorRekening(String nomorRekening);
}
