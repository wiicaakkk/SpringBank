package com.belajar.springboot.gl.service;

import com.belajar.springboot.gl.dto.JurnalRequest;
import com.belajar.springboot.gl.entity.Coa;
import com.belajar.springboot.gl.repository.CoaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class JurnalService {

    private static final AtomicLong SEQUENCE = new AtomicLong(0);

    @Autowired
    private CoaRepository coaRepository;

    @Autowired
    private GlPostingService glPostingService;

    @Transactional
    public String postJurnal(JurnalRequest request, String operatorId) {
        if (request.getKodeAkunDebit().equals(request.getKodeAkunKredit())) {
            throw new RuntimeException("Akun debit dan kredit tidak boleh sama!");
        }

        Coa debit = cariAkun(request.getKodeAkunDebit());
        Coa kredit = cariAkun(request.getKodeAkunKredit());

        String ref = generateRef();
        String namaKeterangan = request.getKeterangan() == null || request.getKeterangan().isBlank()
                ? "Jurnal umum " + debit.getNamaAkun() + " / " + kredit.getNamaAkun()
                : request.getKeterangan();

        glPostingService.post(debit.getKodeAkun(), kredit.getKodeAkun(), request.getNominal(),
                ref, namaKeterangan, operatorId);
        return ref;
    }

    private Coa cariAkun(String kode) {
        return coaRepository.findByKodeAkun(kode)
                .orElseThrow(() -> new RuntimeException("Akun GL " + kode + " tidak ditemukan!"));
    }

    private String generateRef() {
        return "JRL" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
                + String.format("%03d", SEQUENCE.incrementAndGet() % 1000);
    }
}