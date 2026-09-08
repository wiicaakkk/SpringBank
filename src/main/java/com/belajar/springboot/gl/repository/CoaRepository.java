package com.belajar.springboot.gl.repository;

import com.belajar.springboot.gl.entity.Coa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CoaRepository extends JpaRepository<Coa, Long> {

    Optional<Coa> findByKodeAkun(String kodeAkun);

    boolean existsByKodeAkun(String kodeAkun);

    List<Coa> findAllByOrderByKodeAkun();

    List<Coa> findByAktifTrueOrderByKodeAkun();
}