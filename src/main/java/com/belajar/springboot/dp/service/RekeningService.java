package com.belajar.springboot.dp.service;

import com.belajar.springboot.dp.dto.CashOperationRequest;
import com.belajar.springboot.dp.dto.CreateRekeningRequest;
import com.belajar.springboot.dp.dto.RekeningResponse;
import com.belajar.springboot.dp.dto.TransferRequest;
import com.belajar.springboot.dp.entity.Rekening;
import com.belajar.springboot.dp.entity.TipeTransaksi;
import com.belajar.springboot.dp.entity.Transaksi;
import com.belajar.springboot.dp.repository.RekeningRepository;
import com.belajar.springboot.dp.repository.TransaksiRepository;
import com.belajar.springboot.gl.service.GlPostingService;
import com.belajar.springboot.rc.entity.Nasabah;
import com.belajar.springboot.rc.entity.NasabahHistory;
import com.belajar.springboot.rc.repository.NasabahHistoryRepository;
import com.belajar.springboot.rc.repository.NasabahRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class RekeningService {

    @Autowired
    private RekeningRepository rekeningRepository;

    @Autowired
    private NasabahRepository nasabahRepository;

    @Autowired
    private NasabahHistoryRepository nasabahHistoryRepository;

    @Autowired
    private TransaksiRepository transaksiRepository;

    @Autowired
    private GlPostingService glPostingService;

    @Transactional
    public RekeningResponse createRekening(CreateRekeningRequest request, String operatorId) {
        Nasabah nasabah = nasabahRepository.findByCif(request.getCif())
                .orElseThrow(() -> new RuntimeException("Nasabah dengan CIF " + request.getCif() + " tidak ditemukan!"));

        String nomorRekening = generateNomorRekening();

        Rekening rekening = Rekening.builder()
                .nomorRekening(nomorRekening)
                .nasabah(nasabah)
                .jenisTabungan(request.getJenisTabungan())
                .saldo(request.getSetoranAwal())
                .statusRekening("AKTIF")
                .build();

        rekeningRepository.save(rekening);

        if (request.getSetoranAwal() != null && request.getSetoranAwal().compareTo(BigDecimal.ZERO) > 0) {
            glPostingService.post(GlPostingService.KAS, GlPostingService.TABUNGAN, request.getSetoranAwal(),
                    "REK" + nomorRekening, "Pembukaan rekening " + nomorRekening + " (" + request.getJenisTabungan() + ")", operatorId);
        }

        logHistory(nasabah.getCif(), "CREATE_REKENING", 
                String.format("Pembukaan Rekening Baru No: %s (%s) Setoran Awal: Rp %s", 
                        nomorRekening, request.getJenisTabungan(), request.getSetoranAwal()), 
                operatorId);

        return mapToResponse(rekening);
    }

    @Transactional
    public void transfer(TransferRequest request, String operatorId) {
        if (request.getNomorRekeningAsal().equals(request.getNomorRekeningTujuan())) {
            throw new RuntimeException("Nomor rekening asal dan tujuan tidak boleh sama!");
        }

        Rekening rekAsal = rekeningRepository.findByNomorRekening(request.getNomorRekeningAsal())
                .orElseThrow(() -> new RuntimeException("Rekening asal " + request.getNomorRekeningAsal() + " tidak ditemukan!"));

        Rekening rekTujuan = rekeningRepository.findByNomorRekening(request.getNomorRekeningTujuan())
                .orElseThrow(() -> new RuntimeException("Rekening tujuan " + request.getNomorRekeningTujuan() + " tidak ditemukan!"));

        if (!"AKTIF".equals(rekAsal.getStatusRekening())) {
            throw new RuntimeException("Rekening asal tidak dalam status AKTIF!");
        }

        if (!"AKTIF".equals(rekTujuan.getStatusRekening())) {
            throw new RuntimeException("Rekening tujuan tidak dalam status AKTIF!");
        }

        if (rekAsal.getSaldo().compareTo(request.getNominal()) < 0) {
            throw new RuntimeException("Saldo rekening asal tidak mencukupi! Saldo saat ini: Rp " + rekAsal.getSaldo());
        }

        rekAsal.setSaldo(rekAsal.getSaldo().subtract(request.getNominal()));
        rekTujuan.setSaldo(rekTujuan.getSaldo().add(request.getNominal()));

        rekeningRepository.save(rekAsal);
        rekeningRepository.save(rekTujuan);

        glPostingService.post(GlPostingService.TABUNGAN, GlPostingService.TABUNGAN, request.getNominal(),
                "TRF" + request.getNomorRekeningAsal(), "Transfer internal " + request.getNomorRekeningAsal()
                        + " ke " + request.getNomorRekeningTujuan() + ". " + request.getBeritaTransfer(),
                operatorId);

        String ketAsal = String.format("Transfer Keluar Rp %s ke Rek %s. Ket: %s", 
                request.getNominal(), request.getNomorRekeningTujuan(), request.getBeritaTransfer());
        logHistory(rekAsal.getNasabah().getCif(), "TRANSFER_DEBIT", ketAsal, operatorId);

        String ketTujuan = String.format("Transfer Masuk Rp %s dari Rek %s. Ket: %s", 
                request.getNominal(), request.getNomorRekeningAsal(), request.getBeritaTransfer());
        logHistory(rekTujuan.getNasabah().getCif(), "TRANSFER_KREDIT", ketTujuan, operatorId);
    }

    @Transactional
    public RekeningResponse setorTunai(CashOperationRequest request, String operatorId) {
        Rekening rekening = cariRekeningAktif(request.getNomorRekening());

        rekening.setSaldo(rekening.getSaldo().add(request.getNominal()));
        rekeningRepository.save(rekening);

        catatTransaksi(rekening.getNomorRekening(), TipeTransaksi.KREDIT,
                request.getNominal(), "Setoran Tunai", operatorId);

        glPostingService.post(GlPostingService.KAS, GlPostingService.TABUNGAN, request.getNominal(),
                "DP03" + rekening.getNomorRekening(), "Setoran tunai ke rekening " + rekening.getNomorRekening(),
                operatorId);

        logHistory(rekening.getNasabah().getCif(), "DP03_SETORAN_TUNAI",
                String.format("Setoran tunai Rp %s ke Rekening %s", request.getNominal(), request.getNomorRekening()),
                operatorId);

        return mapToResponse(rekening);
    }

    @Transactional
    public RekeningResponse tarikTunai(CashOperationRequest request, String operatorId) {
        Rekening rekening = cariRekeningAktif(request.getNomorRekening());

        if (rekening.getSaldo().compareTo(request.getNominal()) < 0) {
            throw new RuntimeException("Saldo tidak mencukupi untuk tarik tunai! Saldo saat ini: Rp " + rekening.getSaldo());
        }

        rekening.setSaldo(rekening.getSaldo().subtract(request.getNominal()));
        rekeningRepository.save(rekening);

        catatTransaksi(rekening.getNomorRekening(), TipeTransaksi.DEBIT,
                request.getNominal(), "Tarik Tunai", operatorId);

        glPostingService.post(GlPostingService.TABUNGAN, GlPostingService.KAS, request.getNominal(),
                "DP04" + rekening.getNomorRekening(), "Tarik tunai dari rekening " + rekening.getNomorRekening(),
                operatorId);

        logHistory(rekening.getNasabah().getCif(), "DP04_TARIK_TUNAI",
                String.format("Tarik tunai Rp %s dari Rekening %s", request.getNominal(), request.getNomorRekening()),
                operatorId);

        return mapToResponse(rekening);
    }

    @Transactional
    public RekeningResponse tutupRekening(String nomorRekening, String operatorId) {
        Rekening rekening = cariRekeningAktif(nomorRekening);

        if (rekening.getSaldo().compareTo(BigDecimal.ZERO) != 0) {
            throw new RuntimeException("Rekening tidak dapat ditutup karena saldo belum nol! Saldo saat ini: Rp " + rekening.getSaldo());
        }

        rekening.setStatusRekening("CLOSED");
        rekeningRepository.save(rekening);

        logHistory(rekening.getNasabah().getCif(), "DP05_PENUTUPAN",
                "Penutupan rekening " + nomorRekening, operatorId);

        return mapToResponse(rekening);
    }

    @Transactional
    public RekeningResponse blokirRekening(String nomorRekening, String operatorId) {
        Rekening rekening = cariRekeningAktif(nomorRekening);

        rekening.setStatusRekening("BLOCKED");
        rekeningRepository.save(rekening);

        logHistory(rekening.getNasabah().getCif(), "DP06_BLOKIR",
                "Blokir rekening " + nomorRekening, operatorId);

        return mapToResponse(rekening);
    }

    @Transactional
    public RekeningResponse bukaBlokirRekening(String nomorRekening, String operatorId) {
        Rekening rekening = rekeningRepository.findByNomorRekening(nomorRekening)
                .orElseThrow(() -> new RuntimeException("Rekening " + nomorRekening + " tidak ditemukan!"));

        if (!"BLOCKED".equals(rekening.getStatusRekening())) {
            throw new RuntimeException("Rekening " + nomorRekening + " tidak sedang dalam status BLOCKED!");
        }

        rekening.setStatusRekening("AKTIF");
        rekeningRepository.save(rekening);

        logHistory(rekening.getNasabah().getCif(), "DP06_BUKA_BLOKIR",
                "Buka blokir rekening " + nomorRekening, operatorId);

        return mapToResponse(rekening);
    }

    public List<RekeningResponse> getRekeningByCif(String cif) {
        return rekeningRepository.findByNasabahCif(cif).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private String generateNomorRekening() {
        Random random = new Random();
        String rek;
        do {
            long number = 1000000000L + (long)(random.nextDouble() * 9000000000L);
            rek = String.valueOf(number);
        } while (rekeningRepository.existsByNomorRekening(rek));
        return rek;
    }

    private Rekening cariRekeningAktif(String nomorRekening) {
        Rekening rekening = rekeningRepository.findByNomorRekening(nomorRekening)
                .orElseThrow(() -> new RuntimeException("Rekening " + nomorRekening + " tidak ditemukan!"));

        if (!"AKTIF".equals(rekening.getStatusRekening())) {
            throw new RuntimeException("Rekening " + nomorRekening + " tidak dalam status AKTIF!");
        }
        return rekening;
    }

    private void catatTransaksi(String nomorRekening, TipeTransaksi tipe, BigDecimal nominal,
                                String deskripsi, String operatorId) {
        Transaksi transaksi = Transaksi.builder()
                .nomorRekening(nomorRekening)
                .tipeTransaksi(tipe)
                .amount(nominal)
                .deskripsi(deskripsi + " (Petugas: " + operatorId + ")")
                .build();
        transaksiRepository.save(transaksi);
    }

    private void logHistory(String cif, String actionType, String keterangan, String updatedBy) {
        NasabahHistory history = NasabahHistory.builder()
                .cif(cif)
                .actionType(actionType)
                .keterangan(keterangan)
                .updatedBy(updatedBy != null ? updatedBy : "SYSTEM")
                .build();
        nasabahHistoryRepository.save(history);
    }

    private RekeningResponse mapToResponse(Rekening rekening) {
        return RekeningResponse.builder()
                .nomorRekening(rekening.getNomorRekening())
                .cif(rekening.getNasabah().getCif())
                .namaNasabah(rekening.getNasabah().getNamaLengkap())
                .jenisTabungan(rekening.getJenisTabungan())
                .saldo(rekening.getSaldo())
                .statusRekening(rekening.getStatusRekening())
                .createdAt(rekening.getCreatedAt())
                .build();
    }
}
