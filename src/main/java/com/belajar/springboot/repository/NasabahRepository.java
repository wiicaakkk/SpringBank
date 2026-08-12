package com.belajar.springboot.repository;

import com.belajar.springboot.entity.Nasabah;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NasabahRepository extends JpaRepository<Nasabah, Long> {

    Optional<Nasabah> findByCif(String cif);

    Optional<Nasabah> findByNik(String nik);

    boolean existsByNik(String nik);

    boolean existsByEmail(String email);

    @Query("SELECT n FROM Nasabah n WHERE LOWER(n.namaLengkap) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR n.cif LIKE CONCAT('%', :keyword, '%') OR n.nik LIKE CONCAT('%', :keyword, '%')")
    List<Nasabah> searchNasabah(@Param("keyword") String keyword);

    @Query("SELECT COUNT(n) FROM Nasabah n")
    long countTotalNasabah();
}
