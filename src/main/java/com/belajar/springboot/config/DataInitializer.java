package com.belajar.springboot.config;

import com.belajar.springboot.auth.entity.Role;
import com.belajar.springboot.auth.entity.User;
import com.belajar.springboot.auth.repository.UserRepository;
import com.belajar.springboot.dp.entity.JenisTabungan;
import com.belajar.springboot.dp.entity.Rekening;
import com.belajar.springboot.dp.repository.RekeningRepository;
import com.belajar.springboot.gl.entity.Coa;
import com.belajar.springboot.gl.entity.JenisAkun;
import com.belajar.springboot.gl.entity.PosisiNormal;
import com.belajar.springboot.gl.repository.CoaRepository;
import com.belajar.springboot.gl.repository.GlEntryRepository;
import com.belajar.springboot.gl.service.GlPostingService;
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
import java.util.List;

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

    @Autowired
    private com.belajar.springboot.dp.repository.TransaksiRepository transaksiRepository;

    @Autowired
    private CoaRepository coaRepository;

    @Autowired
    private GlEntryRepository glEntryRepository;

    @Autowired
    private GlPostingService glPostingService;

    @Autowired
    private com.belajar.springboot.sb.repository.SafeDepositBoxRepository safeDepositBoxRepository;

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

        if (transaksiRepository.count() == 0) {
            String rek1 = "3436196555";
            String rek2 = "8081350139";

            // Jan 2025
            transaksiRepository.save(com.belajar.springboot.dp.entity.Transaksi.builder()
                    .nomorRekening(rek1)
                    .tipeTransaksi(com.belajar.springboot.dp.entity.TipeTransaksi.KREDIT)
                    .amount(new BigDecimal("5000000.00"))
                    .deskripsi("Gaji Bulanan")
                    .status("SUCCESS")
                    .createdAt(java.time.LocalDateTime.of(2025, 1, 10, 10, 0))
                    .build());
            transaksiRepository.save(com.belajar.springboot.dp.entity.Transaksi.builder()
                    .nomorRekening(rek1)
                    .tipeTransaksi(com.belajar.springboot.dp.entity.TipeTransaksi.TOPUP_PULSA)
                    .amount(new BigDecimal("100000.00"))
                    .deskripsi("Pembelian Pulsa XL 100k")
                    .status("SUCCESS")
                    .createdAt(java.time.LocalDateTime.of(2025, 1, 15, 14, 30))
                    .build());

            // Feb 2025
            transaksiRepository.save(com.belajar.springboot.dp.entity.Transaksi.builder()
                    .nomorRekening(rek1)
                    .tipeTransaksi(com.belajar.springboot.dp.entity.TipeTransaksi.PAKET_DATA)
                    .amount(new BigDecimal("150000.00"))
                    .deskripsi("Paket Data XL Xtra Combo 50GB")
                    .status("SUCCESS")
                    .createdAt(java.time.LocalDateTime.of(2025, 2, 5, 9, 15))
                    .build());
            transaksiRepository.save(com.belajar.springboot.dp.entity.Transaksi.builder()
                    .nomorRekening(rek1)
                    .tipeTransaksi(com.belajar.springboot.dp.entity.TipeTransaksi.DEBIT)
                    .amount(new BigDecimal("500000.00"))
                    .deskripsi("Transfer ke Rekening 8081350139")
                    .status("SUCCESS")
                    .createdAt(java.time.LocalDateTime.of(2025, 2, 14, 18, 20))
                    .build());
            transaksiRepository.save(com.belajar.springboot.dp.entity.Transaksi.builder()
                    .nomorRekening(rek2)
                    .tipeTransaksi(com.belajar.springboot.dp.entity.TipeTransaksi.KREDIT)
                    .amount(new BigDecimal("500000.00"))
                    .deskripsi("Terima Transfer dari 3436196555")
                    .status("SUCCESS")
                    .createdAt(java.time.LocalDateTime.of(2025, 2, 14, 18, 20))
                    .build());

            // Mar 2025
            transaksiRepository.save(com.belajar.springboot.dp.entity.Transaksi.builder()
                    .nomorRekening(rek1)
                    .tipeTransaksi(com.belajar.springboot.dp.entity.TipeTransaksi.KREDIT)
                    .amount(new BigDecimal("7500000.00"))
                    .deskripsi("Bonus Project Backend")
                    .status("SUCCESS")
                    .createdAt(java.time.LocalDateTime.of(2025, 3, 1, 11, 0))
                    .build());
            transaksiRepository.save(com.belajar.springboot.dp.entity.Transaksi.builder()
                    .nomorRekening(rek1)
                    .tipeTransaksi(com.belajar.springboot.dp.entity.TipeTransaksi.PAYMENT)
                    .amount(new BigDecimal("250000.00"))
                    .deskripsi("Pembayaran Tagihan XL Prioritas")
                    .status("SUCCESS")
                    .createdAt(java.time.LocalDateTime.of(2025, 3, 20, 16, 45))
                    .build());

            // Apr 2025
            transaksiRepository.save(com.belajar.springboot.dp.entity.Transaksi.builder()
                    .nomorRekening(rek2)
                    .tipeTransaksi(com.belajar.springboot.dp.entity.TipeTransaksi.TOPUP_PULSA)
                    .amount(new BigDecimal("200000.00"))
                    .deskripsi("Topup Pulsa XL Home")
                    .status("SUCCESS")
                    .createdAt(java.time.LocalDateTime.of(2025, 4, 10, 8, 30))
                    .build());

            System.out.println("[INITIALIZER] Initial Transaksi (Jan 2025 - Apr 2025) Berhasil Seeded!");
        }

        {
            List<Coa> coaList = List.of(
                    Coa.builder().kodeAkun(GlPostingService.KAS).namaAkun("Kas").jenisAkun(JenisAkun.ASET).posisiNormal(PosisiNormal.DEBIT).build(),
                    Coa.builder().kodeAkun(GlPostingService.KAS_KECIL).namaAkun("Kas Kecil").jenisAkun(JenisAkun.ASET).posisiNormal(PosisiNormal.DEBIT).build(),
                    Coa.builder().kodeAkun(GlPostingService.TABUNGAN).namaAkun("Rekening Tabungan").jenisAkun(JenisAkun.KEWAJIBAN).posisiNormal(PosisiNormal.KREDIT).build(),
                    Coa.builder().kodeAkun(GlPostingService.GIRO).namaAkun("Rekening Giro").jenisAkun(JenisAkun.KEWAJIBAN).posisiNormal(PosisiNormal.KREDIT).build(),
                    Coa.builder().kodeAkun(GlPostingService.DEPOSITO).namaAkun("Deposito Berjangka").jenisAkun(JenisAkun.KEWAJIBAN).posisiNormal(PosisiNormal.KREDIT).build(),
                    Coa.builder().kodeAkun(GlPostingService.KREDIT_DIBERIKAN).namaAkun("Kredit yang Diberikan").jenisAkun(JenisAkun.ASET).posisiNormal(PosisiNormal.DEBIT).build(),
                    Coa.builder().kodeAkun(GlPostingService.MODAL_DASAR).namaAkun("Modal Dasar").jenisAkun(JenisAkun.EKUITAS).posisiNormal(PosisiNormal.KREDIT).build(),
                    Coa.builder().kodeAkun(GlPostingService.PENDAPATAN_ADMIN).namaAkun("Pendapatan Administrasi").jenisAkun(JenisAkun.PENDAPATAN).posisiNormal(PosisiNormal.KREDIT).build(),
                    Coa.builder().kodeAkun(GlPostingService.PENDAPATAN_JASA).namaAkun("Pendapatan Jasa").jenisAkun(JenisAkun.PENDAPATAN).posisiNormal(PosisiNormal.KREDIT).build(),
                    Coa.builder().kodeAkun(GlPostingService.BEBAN_OPERASIONAL).namaAkun("Beban Operasional").jenisAkun(JenisAkun.BEBAN).posisiNormal(PosisiNormal.DEBIT).build(),
                    Coa.builder().kodeAkun(GlPostingService.BEBAN_UMUM).namaAkun("Beban Umum dan Administrasi").jenisAkun(JenisAkun.BEBAN).posisiNormal(PosisiNormal.DEBIT).build()
            );
            for (Coa coa : coaList) {
                if (!coaRepository.existsByKodeAkun(coa.getKodeAkun())) {
                    coaRepository.save(coa);
                }
            }
            System.out.println("[INITIALIZER] Chart of Accounts Berhasil Disinkronkan!");
        }

        if (safeDepositBoxRepository.count() == 0) {
            for (int i = 1; i <= 6; i++) {
                String nomor = String.format("SB-%03d", i);
                safeDepositBoxRepository.save(com.belajar.springboot.sb.entity.SafeDepositBox.builder()
                        .nomorBox(nomor)
                        .status(com.belajar.springboot.sb.entity.StatusBox.TERSEDIA)
                        .build());
            }
            System.out.println("[INITIALIZER] 6 Box Safe Deposit Berhasil Diseeded (SB-001 .. SB-006)!");
        }

        if (glEntryRepository.count() == 0) {
            BigDecimal totalSaldo = rekeningRepository.findAll().stream()
                    .map(Rekening::getSaldo)
                    .filter(saldo -> saldo != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (totalSaldo.compareTo(BigDecimal.ZERO) > 0) {
                glPostingService.post(GlPostingService.KAS, GlPostingService.TABUNGAN, totalSaldo,
                        "OPENING", "Saldo pembukaan rekening tabungan (seeding)", "SYSTEM");
                System.out.println("[INITIALIZER] Posting GL Pembukaan Berhasil: " + totalSaldo);
            }
        }
    }
}
