package com.belajar.springboot.gl.service;

import com.belajar.springboot.gl.dto.CoaResponse;
import com.belajar.springboot.gl.dto.NeracaResponse;
import com.belajar.springboot.gl.dto.NeracaRow;
import com.belajar.springboot.gl.entity.Coa;
import com.belajar.springboot.gl.entity.JenisAkun;
import com.belajar.springboot.gl.entity.PosisiNormal;
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
public class NeracaService {

    @Autowired
    private CoaRepository coaRepository;

    @Autowired
    private GlEntryRepository glEntryRepository;

    @Transactional(readOnly = true)
    public NeracaResponse getNeraca(LocalDate tanggal) {
        Map<String, BigDecimal> saldo = saldoPerAkun(tanggal);
        LocalDate tgl = tanggal != null ? tanggal : LocalDate.now();

        List<NeracaRow> aktiva = new ArrayList<>();
        List<NeracaRow> pasiva = new ArrayList<>();
        BigDecimal totalAktiva = BigDecimal.ZERO;
        BigDecimal totalPasiva = BigDecimal.ZERO;
        BigDecimal totalPendapatan = BigDecimal.ZERO;
        BigDecimal totalBeban = BigDecimal.ZERO;

        for (Coa coa : coaRepository.findByAktifTrueOrderByKodeAkun()) {
            BigDecimal saldoAkun = saldo.getOrDefault(coa.getKodeAkun(), BigDecimal.ZERO);
            if (coa.getJenisAkun() == JenisAkun.ASET) {
                aktiva.add(NeracaRow.dariCoa(coa, saldoAkun));
                totalAktiva = totalAktiva.add(saldoAkun);
            } else if (coa.getJenisAkun() == JenisAkun.KEWAJIBAN || coa.getJenisAkun() == JenisAkun.EKUITAS) {
                pasiva.add(NeracaRow.dariCoa(coa, saldoAkun));
                totalPasiva = totalPasiva.add(saldoAkun);
            } else if (coa.getJenisAkun() == JenisAkun.PENDAPATAN) {
                totalPendapatan = totalPendapatan.add(saldoAkun);
            } else if (coa.getJenisAkun() == JenisAkun.BEBAN) {
                totalBeban = totalBeban.add(saldoAkun);
            }
        }

        BigDecimal labaBerjalan = totalPendapatan.subtract(totalBeban);
        if (labaBerjalan.compareTo(BigDecimal.ZERO) != 0) {
            pasiva.add(new NeracaRow("9.9.99.999", "Laba / Rugi Berjalan", labaBerjalan));
            totalPasiva = totalPasiva.add(labaBerjalan);
        }

        return NeracaResponse.builder()
                .tanggal(tgl)
                .aktiva(aktiva)
                .pasiva(pasiva)
                .totalAktiva(totalAktiva)
                .totalPasiva(totalPasiva)
                .selisih(totalAktiva.subtract(totalPasiva))
                .build();
    }

    @Transactional(readOnly = true)
    public List<CoaResponse> daftarAkun(LocalDate tanggal) {
        Map<String, BigDecimal> saldo = saldoPerAkun(tanggal != null ? tanggal : LocalDate.now());

        List<CoaResponse> hasil = new ArrayList<>();
        for (Coa coa : coaRepository.findAllByOrderByKodeAkun()) {
            hasil.add(CoaResponse.builder()
                    .id(coa.getId())
                    .kodeAkun(coa.getKodeAkun())
                    .namaAkun(coa.getNamaAkun())
                    .jenisAkun(coa.getJenisAkun())
                    .posisiNormal(coa.getPosisiNormal())
                    .aktif(coa.getAktif())
                    .saldo(saldo.getOrDefault(coa.getKodeAkun(), BigDecimal.ZERO))
                    .build());
        }
        return hasil;
    }

    private Map<String, BigDecimal> saldoPerAkun(LocalDate tanggal) {
        BigDecimal debit;
        BigDecimal kredit;
        Map<String, BigDecimal> netPerKode = new HashMap<>();
        for (Object[] row : glEntryRepository.sumSisiPerAkunUntil(tanggal)) {
            debit = row[1] == null ? BigDecimal.ZERO : (BigDecimal) row[1];
            kredit = row[2] == null ? BigDecimal.ZERO : (BigDecimal) row[2];
            netPerKode.put((String) row[0], debit.subtract(kredit));
        }

        Map<String, BigDecimal> saldoFinal = new HashMap<>();
        for (Map.Entry<String, BigDecimal> e : netPerKode.entrySet()) {
            Coa coa = coaRepository.findByKodeAkun(e.getKey()).orElse(null);
            BigDecimal nilai = e.getValue();
            if (coa != null && coa.getPosisiNormal() == PosisiNormal.KREDIT) {
                saldoFinal.put(e.getKey(), nilai.negate());
            } else {
                saldoFinal.put(e.getKey(), nilai);
            }
        }
        return saldoFinal;
    }
}