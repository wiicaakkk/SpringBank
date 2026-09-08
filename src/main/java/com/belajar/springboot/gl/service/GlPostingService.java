package com.belajar.springboot.gl.service;

import com.belajar.springboot.gl.entity.Coa;
import com.belajar.springboot.gl.entity.GlEntry;
import com.belajar.springboot.gl.entity.SisiPembukuan;
import com.belajar.springboot.gl.repository.CoaRepository;
import com.belajar.springboot.gl.repository.GlEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class GlPostingService {

    public static final String KAS = "1.1.01.001";
    public static final String KAS_KECIL = "1.1.01.002";
    public static final String TABUNGAN = "1.2.01.001";
    public static final String GIRO = "1.2.02.001";
    public static final String DEPOSITO = "1.2.03.001";
    public static final String KREDIT_DIBERIKAN = "1.5.01.001";
    public static final String MODAL_DASAR = "3.1.01.001";
    public static final String PENDAPATAN_ADMIN = "4.1.01.001";
    public static final String PENDAPATAN_JASA = "4.1.02.001";
    public static final String BEBAN_OPERASIONAL = "5.1.01.001";
    public static final String BEBAN_UMUM = "5.1.02.001";

    @Autowired
    private CoaRepository coaRepository;

    @Autowired
    private GlEntryRepository glEntryRepository;

    @Transactional
    public void post(String kodeDebit, String kodeKredit, BigDecimal nominal,
                     String refTransaksi, String keterangan, String operatorId) {
        validasiPosisi(kodeDebit);
        validasiPosisi(kodeKredit);

        glEntryRepository.save(entry(kodeDebit, SisiPembukuan.DEBIT, nominal, refTransaksi, keterangan, operatorId));
        glEntryRepository.save(entry(kodeKredit, SisiPembukuan.KREDIT, nominal, refTransaksi, keterangan, operatorId));
    }

    private GlEntry entry(String kodeAkun, SisiPembukuan sisi, BigDecimal nominal,
                          String refTransaksi, String keterangan, String operatorId) {
        return GlEntry.builder()
                .kodeAkun(kodeAkun)
                .sisi(sisi)
                .nominal(nominal)
                .refTransaksi(refTransaksi)
                .keterangan(keterangan)
                .operatorId(operatorId != null ? operatorId : "SYSTEM")
                .build();
    }

    private void validasiPosisi(String kodeAkun) {
        if (kodeAkun == null || kodeAkun.isBlank()) {
            throw new RuntimeException("Kode akun GL wajib diisi!");
        }
        Coa coa = coaRepository.findByKodeAkun(kodeAkun)
                .orElseThrow(() -> new RuntimeException("Akun GL " + kodeAkun + " tidak ditemukan!"));
        if (!Boolean.TRUE.equals(coa.getAktif())) {
            throw new RuntimeException("Akun GL " + kodeAkun + " tidak aktif!");
        }
    }
}