package com.belajar.springboot.rc.repository;

import com.belajar.springboot.rc.entity.Nasabah;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NasabahRepository extends JpaRepository<Nasabah, Long> {

    Optional<Nasabah> findByCif(String cif);

    boolean existsByNik(String nik);

    boolean existsByEmail(String email);

    @Query("SELECT n FROM Nasabah n WHERE LOWER(n.namaLengkap) LIKE LOWER(CONCAT('%', :kw, '%')) OR n.nik LIKE CONCAT('%', :kw, '%') OR LOWER(n.cif) LIKE LOWER(CONCAT('%', :kw, '%'))")
    List<Nasabah> searchNasabah(@Param("kw") String keyword);
}
