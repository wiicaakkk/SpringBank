package com.belajar.springboot.dp.service;

import com.belajar.springboot.dp.dto.LaporanHarianResponse;
import com.belajar.springboot.dp.dto.LaporanHarianRow;
import com.belajar.springboot.dp.dto.RingkasanHarian;
import com.belajar.springboot.dp.entity.TipeTransaksi;
import com.belajar.springboot.dp.entity.Transaksi;
import com.belajar.springboot.dp.repository.TransaksiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class LaporanHarianService {

    private static final Pattern PETUGAS = Pattern.compile("Petugas:\\s*([A-Za-z0-9_.-]+)");

    private final TransaksiRepository transaksiRepository;

    @Transactional(readOnly = true)
    public LaporanHarianResponse getLaporanHarian(LocalDate tanggal) {
        LocalDate t = tanggal != null ? tanggal : LocalDate.now();
        List<Transaksi> rows = transaksiRepository.findByCreatedAtBetweenOrderByCreatedAtAsc(
                t.atStartOfDay(), t.atTime(23, 59, 59));

        List<LaporanHarianRow> baris = new ArrayList<>();
        Map<String, long[]> jumlahPerTipe = new LinkedHashMap<>();
        Map<String, BigDecimal> totalPerTipe = new LinkedHashMap<>();
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalKredit = BigDecimal.ZERO;

        for (Transaksi tr : rows) {
            boolean keluar = isDebit(tr.getTipeTransaksi());
            BigDecimal nilai = tr.getAmount() != null ? tr.getAmount() : BigDecimal.ZERO;
            if (keluar) {
                totalDebit = totalDebit.add(nilai);
            } else {
                totalKredit = totalKredit.add(nilai);
            }

            jumlahPerTipe.merge(tr.getTipeTransaksi().name(), new long[]{1}, (a, b) -> new long[]{a[0] + 1});
            totalPerTipe.put(tr.getTipeTransaksi().name(),
                    totalPerTipe.getOrDefault(tr.getTipeTransaksi().name(), BigDecimal.ZERO).add(nilai));

            baris.add(LaporanHarianRow.builder()
                    .tanggal(tr.getCreatedAt())
                    .nomorRekening(tr.getNomorRekening())
                    .tipeTransaksi(tr.getTipeTransaksi().name())
                    .deskripsi(tr.getDeskripsi())
                    .operatorId(ekstrakOperator(tr.getDeskripsi()))
                    .debit(keluar ? nilai : null)
                    .kredit(keluar ? null : nilai)
                    .build());
        }

        List<RingkasanHarian> ringkasan = new ArrayList<>();
        for (Map.Entry<String, long[]> e : jumlahPerTipe.entrySet()) {
            BigDecimal total = totalPerTipe.getOrDefault(e.getKey(), BigDecimal.ZERO);
            boolean keluar = isDebit(TipeTransaksi.valueOf(e.getKey()));
            ringkasan.add(RingkasanHarian.builder()
                    .tipeTransaksi(e.getKey())
                    .jumlah(e.getValue()[0])
                    .totalDebit(keluar ? total : null)
                    .totalKredit(keluar ? null : total)
                    .build());
        }

        return LaporanHarianResponse.builder()
                .tanggal(t)
                .baris(baris)
                .ringkasan(ringkasan)
                .totalTransaksi(rows.size())
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

    private String ekstrakOperator(String deskripsi) {
        if (deskripsi == null) return null;
        Matcher m = PETUGAS.matcher(deskripsi);
        return m.find() ? m.group(1) : null;
    }
}