package com.belajar.springboot.gl.service;

import com.belajar.springboot.gl.dto.LabaRugiResponse;
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
public class LabaRugiService {

    @Autowired
    private CoaRepository coaRepository;

    @Autowired
    private GlEntryRepository glEntryRepository;

    @Transactional(readOnly = true)
    public LabaRugiResponse getLabaRugi(LocalDate tanggal) {
        Map<String, BigDecimal> saldo = saldoPerAkun(tanggal != null ? tanggal : LocalDate.now());
        LocalDate tgl = tanggal != null ? tanggal : LocalDate.now();

        List<NeracaRow> pendapatan = new ArrayList<>();
        List<NeracaRow> beban = new ArrayList<>();
        BigDecimal totalPendapatan = BigDecimal.ZERO;
        BigDecimal totalBeban = BigDecimal.ZERO;

        for (Coa coa : coaRepository.findByAktifTrueOrderByKodeAkun()) {
            BigDecimal saldoAkun = saldo.getOrDefault(coa.getKodeAkun(), BigDecimal.ZERO);
            if (coa.getJenisAkun() == JenisAkun.PENDAPATAN) {
                pendapatan.add(NeracaRow.dariCoa(coa, saldoAkun));
                totalPendapatan = totalPendapatan.add(saldoAkun);
            } else if (coa.getJenisAkun() == JenisAkun.BEBAN) {
                beban.add(NeracaRow.dariCoa(coa, saldoAkun));
                totalBeban = totalBeban.add(saldoAkun);
            }
        }

        return LabaRugiResponse.builder()
                .tanggal(tgl)
                .pendapatan(pendapatan)
                .beban(beban)
                .totalPendapatan(totalPendapatan)
                .totalBeban(totalBeban)
                .labaBersih(totalPendapatan.subtract(totalBeban))
                .build();
    }

    private Map<String, BigDecimal> saldoPerAkun(LocalDate tanggal) {
        Map<String, BigDecimal> netPerKode = new HashMap<>();
        for (Object[] row : glEntryRepository.sumSisiPerAkunUntil(tanggal)) {
            BigDecimal debit = row[1] == null ? BigDecimal.ZERO : (BigDecimal) row[1];
            BigDecimal kredit = row[2] == null ? BigDecimal.ZERO : (BigDecimal) row[2];
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