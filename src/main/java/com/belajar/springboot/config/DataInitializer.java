package com.belajar.springboot.config;

import com.belajar.springboot.auth.entity.Role;
import com.belajar.springboot.auth.entity.User;
import com.belajar.springboot.auth.repository.UserRepository;
import com.belajar.springboot.dp.entity.JenisTabungan;
import com.belajar.springboot.dp.entity.Rekening;
import com.belajar.springboot.dp.repository.RekeningRepository;
import com.belajar.springboot.rc.entity.JenisKelamin;
import com.belajar.springboot.rc.entity.Nasabah;
import com.belajar.springboot.rc.entity.NasabahHistory;
import com.belajar.springboot.rc.entity.StatusNasabah;
import com.belajar.springboot.rc.repository.NasabahHistoryRepository;
import com.belajar.springboot.rc.repository.NasabahRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NasabahRepository nasabahRepository;

    @Autowired
    private NasabahHistoryRepository nasabahHistoryRepository;

    @Autowired
    private RekeningRepository rekeningRepository;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            userRepository.save(new User("teller1", "password123", "Budi Teller", "TLR001", Role.TELLER));
            userRepository.save(new User("spv1", "password123", "Siti Supervisor", "SPV001", Role.SUPERVISOR));
            userRepository.save(new User("admin1", "password123", "Administrator Bank", "ADM001", Role.ADMIN));
            System.out.println("[INITIALIZER] Demo Users Berhasil Dibuat (teller1, spv1, admin1)!");
        }

        if (nasabahRepository.count() == 0) {
            Nasabah n1 = Nasabah.builder()
                    .cif("CIF202607260001")
                    .nik("3171011508900001")
                    .namaLengkap("Budi Santoso")
                    .tempatLahir("Jakarta")
                    .tanggalLahir(LocalDate.of(1990, 8, 15))
                    .jenisKelamin(JenisKelamin.LAKI_LAKI)
                    .ibuKandung("Siti Aminah")
                    .alamat("Jl. Sudirman No. 45, Jakarta Selatan")
                    .nomorHp("081234567890")
                    .email("budi.santoso@email.com")
                    .pekerjaan("Software Engineer")
                    .penghasilanBulanan(new BigDecimal("12000000.00"))
                    .statusNasabah(StatusNasabah.AKTIF)
                    .build();

            Nasabah n2 = Nasabah.builder()
                    .cif("CIF202607260002")
                    .nik("3171012005950002")
                    .namaLengkap("Siti Aminah")
                    .tempatLahir("Bandung")
                    .tanggalLahir(LocalDate.of(1995, 5, 20))
                    .jenisKelamin(JenisKelamin.PEREMPUAN)
                    .ibuKandung("Dewi Lestari")
                    .alamat("Jl. Asia Afrika No. 12, Bandung")
                    .nomorHp("089876543210")
                    .email("siti.aminah@email.com")
                    .pekerjaan("Financial Analyst")
                    .penghasilanBulanan(new BigDecimal("15000000.00"))
                    .statusNasabah(StatusNasabah.AKTIF)
                    .build();

            nasabahRepository.save(n1);
            nasabahRepository.save(n2);

            nasabahHistoryRepository.save(NasabahHistory.builder()
                    .cif("CIF202607260001")
                    .actionType("INITIAL_SEED")
                    .keterangan("Seeding data nasabah Budi Santoso saat insialisasi sistem")
                    .updatedBy("SYSTEM_INITIALIZER")
                    .build());

            nasabahHistoryRepository.save(NasabahHistory.builder()
                    .cif("CIF202607260002")
                    .actionType("INITIAL_SEED")
                    .keterangan("Seeding data nasabah Siti Aminah saat insialisasi sistem")
                    .updatedBy("SYSTEM_INITIALIZER")
                    .build());

            rekeningRepository.save(Rekening.builder()
                    .nomorRekening("3436196555")
                    .nasabah(n1)
                    .jenisTabungan(JenisTabungan.TABUNGAN_UTAMA)
                    .saldo(new BigDecimal("25000000.00"))
                    .statusRekening("AKTIF")
                    .build());

            rekeningRepository.save(Rekening.builder()
                    .nomorRekening("8081350139")
                    .nasabah(n2)
                    .jenisTabungan(JenisTabungan.TABUNGAN_BISNIS)
                    .saldo(new BigDecimal("50000000.00"))
                    .statusRekening("AKTIF")
                    .build());

            System.out.println("[INITIALIZER] Initial Nasabah (Budi Santoso & Siti Aminah) & Rekening Berhasil Seeded!");
        }
    }
}
