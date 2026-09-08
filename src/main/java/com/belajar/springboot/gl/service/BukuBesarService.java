package com.belajar.springboot.gl.service;

import com.belajar.springboot.gl.dto.BukuBesarResponse;
import com.belajar.springboot.gl.dto.BukuBesarRow;
import com.belajar.springboot.gl.entity.Coa;
import com.belajar.springboot.gl.entity.GlEntry;
import com.belajar.springboot.gl.entity.PosisiNormal;
import com.belajar.springboot.gl.entity.SisiPembukuan;
import com.belajar.springboot.gl.repository.CoaRepository;
import com.belajar.springboot.gl.repository.GlEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BukuBesarService {

    @Autowired
    private GlEntryRepository glEntryRepository;

    @Autowired
    private CoaRepository coaRepository;

    @Transactional(readOnly = true)
    public BukuBesarResponse getBukuBesar(String kodeAkun, LocalDate dari, LocalDate sampai) {
        LocalDate tglDari = dari != null ? dari : LocalDate.now().minusDays(30);
        LocalDate tglSampai = sampai != null ? sampai : LocalDate.now();

        List<GlEntry> entries;
        String kode = kodeAkun == null || kodeAkun.isBlank() ? null : kodeAkun.trim();
        if (kode == null) {
            entries = glEntryRepository.findByTanggalPembukuanBetweenOrderByTanggalPembukuanAscIdAsc(tglDari, tglSampai);
        } else {
            entries = glEntryRepository.findByKodeAkunAndTanggalPembukuanBetweenOrderByTanggalPembukuanAscIdAsc(kode, tglDari, tglSampai);
        }

        Map<String, BigDecimal> saldoNormal = saldoNormalPada(tglDari.minusDays(1));
        Map<String, BigDecimal> totalDebitPerAkun = new HashMap<>();
        Map<String, BigDecimal> totalKreditPerAkun = new HashMap<>();

        List<BukuBesarRow> baris = new ArrayList<>();
        for (GlEntry e : entries) {
            Coa coa = coaRepository.findByKodeAkun(e.getKodeAkun()).orElse(null);
            String nama = coa != null ? coa.getNamaAkun() : e.getKodeAkun();
            PosisiNormal pn = coa != null ? coa.getPosisiNormal() : PosisiNormal.DEBIT;

            BigDecimal saldo = saldoNormal.getOrDefault(e.getKodeAkun(), BigDecimal.ZERO);
            if (e.getSisi() == SisiPembukuan.DEBIT) {
                saldo = pn == PosisiNormal.DEBIT
                        ? saldo.add(e.getNominal())
                        : saldo.subtract(e.getNominal());
            } else {
                saldo = pn == PosisiNormal.KREDIT
                        ? saldo.add(e.getNominal())
                        : saldo.subtract(e.getNominal());
            }
            saldoNormal.put(e.getKodeAkun(), saldo);

            if (e.getSisi() == SisiPembukuan.DEBIT) {
                totalDebitPerAkun.merge(e.getKodeAkun(), e.getNominal(), BigDecimal::add);
            } else {
                totalKreditPerAkun.merge(e.getKodeAkun(), e.getNominal(), BigDecimal::add);
            }

            baris.add(BukuBesarRow.builder()
                    .kodeAkun(e.getKodeAkun())
                    .namaAkun(nama)
                    .tanggal(e.getTanggalPembukuan())
                    .sisi(e.getSisi().name())
                    .nominal(e.getNominal())
                    .refTransaksi(e.getRefTransaksi())
                    .keterangan(e.getKeterangan())
                    .operatorId(e.getOperatorId())
                    .saldo(saldo)
                    .build());
        }

        BukuBesarResponse.BukuBesarResponseBuilder b = BukuBesarResponse.builder()
                .tanggalDari(tglDari)
                .tanggalSampai(tglSampai)
                .kodeAkun(kode != null ? kode : "SEMUA AKUN")
                .namaAkun(kode != null ? namaAkun(kode) : null)
                .baris(baris);

        if (kode != null) {
            BigDecimal debit = totalDebitPerAkun.getOrDefault(kode, BigDecimal.ZERO);
            BigDecimal kredit = totalKreditPerAkun.getOrDefault(kode, BigDecimal.ZERO);
            b.totalDebit(debit)
                    .totalKredit(kredit)
                    .saldoAkhir(saldoNormal.getOrDefault(kode, BigDecimal.ZERO));
        }
        return b.build();
    }

    private String namaAkun(String kode) {
        return coaRepository.findByKodeAkun(kode).map(Coa::getNamaAkun).orElse(kode);
    }

    private Map<String, BigDecimal> saldoNormalPada(LocalDate sampai) {
        Map<String, BigDecimal> normal = new HashMap<>();
        for (Object[] row : glEntryRepository.sumSisiPerAkunUntil(sampai)) {
            BigDecimal debit = row[1] == null ? BigDecimal.ZERO : (BigDecimal) row[1];
            BigDecimal kredit = row[2] == null ? BigDecimal.ZERO : (BigDecimal) row[2];
            Coa coa = coaRepository.findByKodeAkun((String) row[0]).orElse(null);
            if (coa != null && coa.getPosisiNormal() == PosisiNormal.KREDIT) {
                normal.put((String) row[0], kredit.subtract(debit));
            } else {
                normal.put((String) row[0], debit.subtract(kredit));
            }
        }
        return normal;
    }
}