package com.belajar.springboot.at.service;

import com.belajar.springboot.at.dto.AutoTransferHistoryResponse;
import com.belajar.springboot.at.dto.AutoTransferResponse;
import com.belajar.springboot.at.dto.ExecuteAutoTransferResponse;
import com.belajar.springboot.at.dto.RegisterAutoTransferRequest;
import com.belajar.springboot.at.entity.AtPeriode;
import com.belajar.springboot.at.entity.AtStatusInstruksi;
import com.belajar.springboot.at.entity.AutoTransfer;
import com.belajar.springboot.at.entity.AutoTransferHistory;
import com.belajar.springboot.at.repository.AutoTransferHistoryRepository;
import com.belajar.springboot.at.repository.AutoTransferRepository;
import com.belajar.springboot.dp.entity.Rekening;
import com.belajar.springboot.dp.entity.TipeTransaksi;
import com.belajar.springboot.dp.entity.Transaksi;
import com.belajar.springboot.dp.repository.RekeningRepository;
import com.belajar.springboot.dp.repository.TransaksiRepository;
import com.belajar.springboot.gl.service.GlPostingService;
import com.belajar.springboot.rc.repository.NasabahHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AutoTransferService {

    @Autowired
    private AutoTransferRepository autoTransferRepository;

    @Autowired
    private AutoTransferHistoryRepository historyRepository;

    @Autowired
    private RekeningRepository rekeningRepository;

    @Autowired
    private TransaksiRepository transaksiRepository;

    @Autowired
    private NasabahHistoryRepository nasabahHistoryRepository;

    @Autowired
    private GlPostingService glPostingService;

    @Transactional
    public AutoTransferResponse register(RegisterAutoTransferRequest request, String operatorId) {
        if (request.getNomorRekeningDebit().equals(request.getNomorRekeningKredit())) {
            throw new RuntimeException("Rekening sumber dan tujuan tidak boleh sama!");
        }

        rekeningAktif(request.getNomorRekeningDebit());
        rekeningAktif(request.getNomorRekeningKredit());

        AtPeriode periode;
        try {
            periode = AtPeriode.valueOf(request.getPeriode().trim().toUpperCase());
        } catch (Exception e) {
            throw new RuntimeException("Periode tidak valid. Gunakan HARIAN, MINGGUAN, atau BULANAN.");
        }

        LocalDate mulai = request.getTanggalMulai() != null ? request.getTanggalMulai() : LocalDate.now();
        if (mulai.isBefore(LocalDate.now())) {
            throw new RuntimeException("Tanggal mulai tidak boleh sebelum hari ini!");
        }

        String kode = generateKodeInstruksi();

        AutoTransfer autoTransfer = AutoTransfer.builder()
                .kodeInstruksi(kode)
                .nomorRekeningDebit(request.getNomorRekeningDebit())
                .nomorRekeningKredit(request.getNomorRekeningKredit())
                .nominal(request.getNominal())
                .periode(periode)
                .tanggalMulai(mulai)
                .tanggalBerikutnya(mulai)
                .statusAt(AtStatusInstruksi.AKTIF)
                .operatorDibuat(operatorId != null ? operatorId : "SYSTEM")
                .build();

        autoTransferRepository.save(autoTransfer);

        autoTransferRepository.findByKodeInstruksi(kode).ifPresent(at ->
                logNasabah(at.getNomorRekeningDebit(), "AT_REGISTER",
                        String.format("Instruksi auto transfer %s terdaftar: %s ke %s, Rp %s, per %s",
                                kode, at.getNomorRekeningDebit(), at.getNomorRekeningKredit(),
                                at.getNominal(), at.getPeriode()), operatorId));

        return mapToResponse(autoTransfer);
    }

    public List<AutoTransferResponse> list(String status) {
        List<AutoTransfer> list;
        if (status != null && !status.isBlank()) {
            AtStatusInstruksi st;
            try {
                st = AtStatusInstruksi.valueOf(status.trim().toUpperCase());
            } catch (Exception e) {
                throw new RuntimeException("Filter status tidak valid. Gunakan AKTIF atau BERHENTI.");
            }
            list = autoTransferRepository.findByStatusAtOrderByCreatedAtDesc(st);
        } else {
            list = autoTransferRepository.findAllByOrderByCreatedAtDesc();
        }
        return list.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public AutoTransferResponse stop(String kodeInstruksi, String operatorId) {
        AutoTransfer at = cari(kodeInstruksi);
        at.setStatusAt(AtStatusInstruksi.BERHENTI);
        autoTransferRepository.save(at);
        logNasabah(at.getNomorRekeningDebit(), "AT_STOP",
                "Instruksi auto transfer " + kodeInstruksi + " dihentikan", operatorId);
        return mapToResponse(at);
    }

    @Transactional
    public AutoTransferResponse start(String kodeInstruksi, String operatorId) {
        AutoTransfer at = cari(kodeInstruksi);
        at.setStatusAt(AtStatusInstruksi.AKTIF);
        if (at.getTanggalBerikutnya().isBefore(LocalDate.now())) {
            at.setTanggalBerikutnya(LocalDate.now());
        }
        autoTransferRepository.save(at);
        logNasabah(at.getNomorRekeningDebit(), "AT_START",
                "Instruksi auto transfer " + kodeInstruksi + " diaktifkan kembali", operatorId);
        return mapToResponse(at);
    }

    public List<AutoTransferHistoryResponse> history(String kodeInstruksi) {
        return historyRepository.findByKodeInstruksiOrderByTanggalEksekusiDesc(kodeInstruksi)
                .stream().map(this::mapHistory).collect(Collectors.toList());
    }

    @Transactional
    public ExecuteAutoTransferResponse execute(String kodeInstruksi, String operatorId) {
        String operator = operatorId != null && !operatorId.isBlank() ? operatorId : "SYSTEM";

        List<AutoTransfer> due;
        if (kodeInstruksi != null && !kodeInstruksi.isBlank()) {
            AutoTransfer at = cari(kodeInstruksi);
            if (at.getStatusAt() != AtStatusInstruksi.AKTIF) {
                throw new RuntimeException("Instruksi " + kodeInstruksi + " tidak dalam status AKTIF.");
            }
            due = List.of(at);
        } else {
            due = autoTransferRepository
                    .findByStatusAtAndTanggalBerikutnyaLessThanEqual(AtStatusInstruksi.AKTIF, LocalDate.now());
        }

        ExecuteAutoTransferResponse result = ExecuteAutoTransferResponse.builder().build();
        for (AutoTransfer at : due) {
            executeOne(at, operator, result);
        }
        return result;
    }

    private void executeOne(AutoTransfer at, String operator, ExecuteAutoTransferResponse result) {
        try {
            Rekening rekDebit = rekeningAktif(at.getNomorRekeningDebit());
            Rekening rekKredit = rekeningAktif(at.getNomorRekeningKredit());

            if (rekDebit.getSaldo().compareTo(at.getNominal()) < 0) {
                throw new RuntimeException("Saldo rekening " + at.getNomorRekeningDebit()
                        + " tidak mencukupi. Saldo: Rp " + rekDebit.getSaldo());
            }

            rekDebit.setSaldo(rekDebit.getSaldo().subtract(at.getNominal()));
            rekKredit.setSaldo(rekKredit.getSaldo().add(at.getNominal()));
            rekeningRepository.save(rekDebit);
            rekeningRepository.save(rekKredit);

            catatTransaksi(at.getNomorRekeningDebit(), TipeTransaksi.DEBIT, at.getNominal(),
                    "Auto Transfer ke Rek " + at.getNomorRekeningKredit() + " (" + at.getKodeInstruksi() + ")", operator);
            catatTransaksi(at.getNomorRekeningKredit(), TipeTransaksi.KREDIT, at.getNominal(),
                    "Penerimaan Auto Transfer dari Rek " + at.getNomorRekeningDebit() + " (" + at.getKodeInstruksi() + ")", operator);

            glPostingService.post(GlPostingService.TABUNGAN, GlPostingService.TABUNGAN, at.getNominal(),
                    at.getKodeInstruksi(), "Auto transfer " + at.getKodeInstruksi() + " dari "
                            + at.getNomorRekeningDebit() + " ke " + at.getNomorRekeningKredit(), operator);

            logNasabah(rekDebit.getNasabah().getCif(), "AT_DEBIT",
                    String.format("Auto transfer %s: Rp %s ke %s", at.getKodeInstruksi(), at.getNominal(),
                            at.getNomorRekeningKredit()), operator);
            logNasabah(rekKredit.getNasabah().getCif(), "AT_KREDIT",
                    String.format("Auto transfer %s: terima Rp %s dari %s", at.getKodeInstruksi(), at.getNominal(),
                            at.getNomorRekeningDebit()), operator);

            tulisHistory(at, LocalDateTime.now(), "SUKSES",
                    String.format("Transfer Rp %s ke %s berhasil", at.getNominal(), at.getNomorRekeningKredit()),
                    operator);
            result.setSukses(result.getSukses() + 1);
            result.getRincian().add(at.getKodeInstruksi() + " SUKSES: Rp " + at.getNominal());
        } catch (RuntimeException e) {
            tulisHistory(at, LocalDateTime.now(), "GAGAL", e.getMessage(), operator);
            result.setGagal(result.getGagal() + 1);
            result.getRincian().add(at.getKodeInstruksi() + " GAGAL: " + e.getMessage());
        } finally {
            result.setTerproses(result.getTerproses() + 1);
            at.setTanggalBerikutnya(berikutnya(at.getTanggalBerikutnya(), at.getPeriode()));
            autoTransferRepository.save(at);
        }
    }

    private AutoTransfer cari(String kodeInstruksi) {
        return autoTransferRepository.findByKodeInstruksi(kodeInstruksi)
                .orElseThrow(() -> new RuntimeException("Instruksi auto transfer " + kodeInstruksi + " tidak ditemukan!"));
    }

    private Rekening rekeningAktif(String nomorRekening) {
        Rekening rekening = rekeningRepository.findByNomorRekening(nomorRekening)
                .orElseThrow(() -> new RuntimeException("Rekening " + nomorRekening + " tidak ditemukan!"));
        if (!"AKTIF".equals(rekening.getStatusRekening())) {
            throw new RuntimeException("Rekening " + nomorRekening + " tidak dalam status AKTIF!");
        }
        return rekening;
    }

    private String generateKodeInstruksi() {
        String prefix = "AT" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        long nomor = autoTransferRepository.countByKodeInstruksiStartingWith(prefix) + 1;
        String kode;
        do {
            kode = prefix + String.format("%03d", nomor++);
        } while (autoTransferRepository.existsByKodeInstruksi(kode));
        return kode;
    }

    private LocalDate berikutnya(LocalDate tanggal, AtPeriode periode) {
        if (periode == AtPeriode.MINGGUAN) return tanggal.plusWeeks(1);
        if (periode == AtPeriode.BULANAN) return tanggal.plusMonths(1);
        return tanggal.plusDays(1);
    }

    private void tulisHistory(AutoTransfer at, LocalDateTime waktu, String status, String keterangan, String operator) {
        historyRepository.save(AutoTransferHistory.builder()
                .kodeInstruksi(at.getKodeInstruksi())
                .nomorRekeningDebit(at.getNomorRekeningDebit())
                .nomorRekeningKredit(at.getNomorRekeningKredit())
                .nominal(at.getNominal())
                .tanggalEksekusi(waktu)
                .statusEksekusi(status)
                .keterangan(keterangan)
                .operatorEksekusi(operator)
                .build());
    }

    private void catatTransaksi(String nomorRekening, TipeTransaksi tipe, BigDecimal nominal,
                                String deskripsi, String operator) {
        transaksiRepository.save(Transaksi.builder()
                .nomorRekening(nomorRekening)
                .tipeTransaksi(tipe)
                .amount(nominal)
                .deskripsi(deskripsi + " (Petugas: " + operator + ")")
                .build());
    }

    private void logNasabah(String cif, String actionType, String keterangan, String operator) {
        nasabahHistoryRepository.save(com.belajar.springboot.rc.entity.NasabahHistory.builder()
                .cif(cif)
                .actionType(actionType)
                .keterangan(keterangan)
                .updatedBy(operator != null ? operator : "SYSTEM")
                .build());
    }

    private AutoTransferResponse mapToResponse(AutoTransfer at) {
        return AutoTransferResponse.builder()
                .kodeInstruksi(at.getKodeInstruksi())
                .nomorRekeningDebit(at.getNomorRekeningDebit())
                .nomorRekeningKredit(at.getNomorRekeningKredit())
                .nominal(at.getNominal())
                .periode(at.getPeriode().name())
                .tanggalMulai(at.getTanggalMulai())
                .tanggalBerikutnya(at.getTanggalBerikutnya())
                .statusAt(at.getStatusAt().name())
                .operatorDibuat(at.getOperatorDibuat())
                .createdAt(at.getCreatedAt())
                .build();
    }

    private AutoTransferHistoryResponse mapHistory(AutoTransferHistory h) {
        return AutoTransferHistoryResponse.builder()
                .kodeInstruksi(h.getKodeInstruksi())
                .nominal(h.getNominal())
                .tanggalEksekusi(h.getTanggalEksekusi())
                .statusEksekusi(h.getStatusEksekusi())
                .keterangan(h.getKeterangan())
                .operatorEksekusi(h.getOperatorEksekusi())
                .build();
    }
}