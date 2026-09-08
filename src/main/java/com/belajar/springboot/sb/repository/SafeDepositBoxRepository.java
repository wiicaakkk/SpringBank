package com.belajar.springboot.sb.repository;

import com.belajar.springboot.sb.entity.SafeDepositBox;
import com.belajar.springboot.sb.entity.StatusBox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SafeDepositBoxRepository extends JpaRepository<SafeDepositBox, Long> {

    Optional<SafeDepositBox> findByNomorBox(String nomorBox);

    List<SafeDepositBox> findByStatusOrderByNomorBoxAsc(StatusBox status);

    List<SafeDepositBox> findAllByOrderByNomorBoxAsc();
}