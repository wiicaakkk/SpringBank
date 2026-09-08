package com.belajar.springboot.dp.service;

import com.belajar.springboot.dp.dto.CreateTransaksiRequest;
import com.belajar.springboot.dp.dto.MutasiResponse;
import com.belajar.springboot.dp.dto.MutasiRowResponse;
import com.belajar.springboot.dp.dto.TransaksiSummaryProjection;
import com.belajar.springboot.dp.dto.TransaksiSummaryResponse;
import com.belajar.springboot.dp.entity.Rekening;
import com.belajar.springboot.dp.entity.TipeTransaksi;
import com.belajar.springboot.dp.entity.Transaksi;
import com.belajar.springboot.dp.repository.RekeningRepository;
import com.belajar.springboot.dp.repository.TransaksiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransaksiService {

    private final TransaksiRepository transaksiRepository;
    private final RekeningRepository rekeningRepository;

    @Transactional(readOnly = true)
    public List<TransaksiSummaryResponse> getMonthlySummary(Integer year, String nomorRekening) {
        List<TransaksiSummaryProjection> projections = transaksiRepository.getMonthlySummary(year, nomorRekening);

        return projections.stream().map(p -> {
            String namaBulan = Month.of(p.getBulan())
                    .getDisplayName(TextStyle.SHORT, Locale.ENGLISH); // e.g. "Feb", "Mar"
            String periode = namaBulan + " " + p.getTahun(); // e.g. "Feb 2025"

            return TransaksiSummaryResponse.builder()
                    .tahun(p.getTahun())
                    .bulan(p.getBulan())
                    .periode(periode)
                    .totalTransaksi(p.getTotalCount())
                    .totalNominal(p.getTotalAmount())
                    .totalDebit(p.getTotalDebit())
                    .totalKredit(p.getTotalKredit())
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional
    public Transaksi createTransaksi(CreateTransaksiRequest request) {
        Transaksi transaksi = Transaksi.builder()
                .nomorRekening(request.getNomorRekening())
                .tipeTransaksi(request.getTipeTransaksi())
                .amount(request.getAmount())
                .deskripsi(request.getDeskripsi())
                .createdAt(request.getCreatedAt())
                .build();

        return transaksiRepository.save(transaksi);
    }

    @Transactional(readOnly = true)
    public List<Transaksi> getHistory(String nomorRekening) {
        if (nomorRekening != null && !nomorRekening.isBlank()) {
            return transaksiRepository.findByNomorRekeningOrderByCreatedAtDesc(nomorRekening);
        }
        return transaksiRepository.findAll();
    }

    @Transactional(readOnly = true)
    public MutasiResponse getMutasi(String nomorRekening, LocalDate dari, LocalDate sampai) {
        LocalDate tglDari = dari != null ? dari : LocalDate.now().minusDays(30);
        LocalDate tglSampai = sampai != null ? sampai : LocalDate.now();

        Rekening rekening = rekeningRepository.findByNomorRekening(nomorRekening)
                .orElseThrow(() -> new RuntimeException("Rekening " + nomorRekening + " tidak ditemukan!"));

        List<Transaksi> rows = transaksiRepository.findByNomorRekeningAndCreatedAtBetweenOrderByCreatedAtAsc(
                nomorRekening, tglDari.atStartOfDay(), tglSampai.atTime(23, 59, 59));

        List<MutasiRowResponse> baris = new ArrayList<>();
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalKredit = BigDecimal.ZERO;
        for (Transaksi t : rows) {
            boolean keluar = isDebit(t.getTipeTransaksi());
            BigDecimal nilai = t.getAmount() != null ? t.getAmount() : BigDecimal.ZERO;
            if (keluar) {
                totalDebit = totalDebit.add(nilai);
            } else {
                totalKredit = totalKredit.add(nilai);
            }
            baris.add(MutasiRowResponse.builder()
                    .tanggal(t.getCreatedAt())
                    .tipeTransaksi(t.getTipeTransaksi().name())
                    .deskripsi(t.getDeskripsi())
                    .debit(keluar ? nilai : null)
                    .kredit(keluar ? null : nilai)
                    .build());
        }
        String nama = rekening.getNasabah() != null ? rekening.getNasabah().getNamaLengkap() : null;
        return MutasiResponse.builder()
                .nomorRekening(nomorRekening)
                .namaNasabah(nama)
                .saldoAkhir(rekening.getSaldo())
                .tanggalDari(tglDari)
                .tanggalSampai(tglSampai)
                .baris(baris)
                .totalDebit(totalDebit)
                .totalKredit(totalKredit)
                .build();
    }

    private boolean isDebit(TipeTransaksi tipe) {
        return tipe == TipeTransaksi.DEBIT
                || tipe == TipeTransaksi.TOPUP_PULSA
                || tipe == TipeTransaksi.PAKET_DATA
                || tipe == TipeTransaksi.PAYMENT;
    }
}
