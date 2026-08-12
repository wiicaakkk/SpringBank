package com.belajar.springboot.rc.repository;

import com.belajar.springboot.rc.entity.NasabahHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NasabahHistoryRepository extends JpaRepository<NasabahHistory, Long> {

    List<NasabahHistory> findByCifOrderByCreatedAtDesc(String cif);

    List<NasabahHistory> findAllByOrderByCreatedAtDesc();
}
