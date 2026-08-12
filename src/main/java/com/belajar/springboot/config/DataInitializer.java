package com.belajar.springboot.config;

import com.belajar.springboot.entity.*;
import com.belajar.springboot.repository.NasabahHistoryRepository;
import com.belajar.springboot.repository.NasabahRepository;
import com.belajar.springboot.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDate;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(NasabahRepository repository, 
                                       NasabahHistoryRepository historyRepository,
                                       UserRepository userRepository) {
        return args -> {
            // Seed Default Banking System Users
            if (userRepository.count() == 0) {
                userRepository.save(new User("teller1", "password123", "Budi Teller", "TLR-1001", Role.TELLER));
                userRepository.save(new User("spv1", "password123", "Siti Supervisor", "SPV-2001", Role.SUPERVISOR));
                userRepository.save(new User("admin1", "password123", "Administrator Core", "ADM-9001", Role.ADMIN));
                System.out.println(">>> Banking Users Initialized: teller1, spv1, admin1!");
            }

            if (repository.count() == 0) {
                repository.save(Nasabah.builder()
                        .cif("CIF202607260001")
                        .nik("3171012005900001")
                        .namaLengkap("Budi Santoso")
                        .tempatLahir("Jakarta")
                        .tanggalLahir(LocalDate.of(1990, 5, 20))
                        .jenisKelamin(JenisKelamin.LAKI_LAKI)
                        .ibuKandung("Siti Fatimah")
                        .alamat("Jl. Sudirman No. 45, Jakarta Selatan")
                        .nomorHp("081234567890")
                        .email("budi.santoso@bank.com")
                        .pekerjaan("Software Engineer")
                        .penghasilanBulanan(new BigDecimal("18500000"))
                        .statusNasabah(StatusNasabah.AKTIF)
                        .build());

                repository.save(Nasabah.builder()
                        .cif("CIF202607260002")
                        .nik("3273021508920003")
                        .namaLengkap("Siti Aminah")
                        .tempatLahir("Bandung")
                        .tanggalLahir(LocalDate.of(1992, 8, 15))
                        .jenisKelamin(JenisKelamin.PEREMPUAN)
                        .ibuKandung("Kartini")
                        .alamat("Jl. Dago No. 102, Bandung")
                        .nomorHp("085678901234")
                        .email("siti.aminah@bank.com")
                        .pekerjaan("Financial Analyst")
                        .penghasilanBulanan(new BigDecimal("22000000"))
                        .statusNasabah(StatusNasabah.AKTIF)
                        .build());

                repository.save(Nasabah.builder()
                        .cif("CIF202607260003")
                        .nik("3578031012880005")
                        .namaLengkap("Andi Wijaya")
                        .tempatLahir("Surabaya")
                        .tanggalLahir(LocalDate.of(1988, 12, 10))
                        .jenisKelamin(JenisKelamin.LAKI_LAKI)
                        .ibuKandung("Mariam")
                        .alamat("Jl. Pemuda No. 12, Surabaya")
                        .nomorHp("081901234567")
                        .email("andi.wijaya@bank.com")
                        .pekerjaan("Pengusaha")
                        .penghasilanBulanan(new BigDecimal("45000000"))
                        .statusNasabah(StatusNasabah.AKTIF)
                        .build());

                // Seed initial history
                historyRepository.save(NasabahHistory.builder()
                        .cif("CIF202607260001")
                        .actionType("REGISTRATION")
                        .keterangan("Pendaftaran Nasabah Baru (Initial Data). Nama: Budi Santoso")
                        .updatedBy("teller1")
                        .build());

                historyRepository.save(NasabahHistory.builder()
                        .cif("CIF202607260002")
                        .actionType("REGISTRATION")
                        .keterangan("Pendaftaran Nasabah Baru (Initial Data). Nama: Siti Aminah")
                        .updatedBy("teller1")
                        .build());

                historyRepository.save(NasabahHistory.builder()
                        .cif("CIF202607260003")
                        .actionType("REGISTRATION")
                        .keterangan("Pendaftaran Nasabah Baru (Initial Data). Nama: Andi Wijaya")
                        .updatedBy("teller1")
                        .build());

                System.out.println(">>> Core Banking Data & Audit Trail Seeding Selesai!");
            }
        };
    }
}

