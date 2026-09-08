package com.belajar.springboot.gl.service;

import com.belajar.springboot.gl.dto.CreateCoaRequest;
import com.belajar.springboot.gl.entity.Coa;
import com.belajar.springboot.gl.repository.CoaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CoaService {

    @Autowired
    private CoaRepository coaRepository;

    @Transactional
    public Coa create(CreateCoaRequest request) {
        if (coaRepository.existsByKodeAkun(request.getKodeAkun())) {
            throw new RuntimeException("Kode akun " + request.getKodeAkun() + " sudah terdaftar!");
        }

        Coa coa = Coa.builder()
                .kodeAkun(request.getKodeAkun())
                .namaAkun(request.getNamaAkun())
                .jenisAkun(request.getJenisAkun())
                .posisiNormal(request.getPosisiNormal())
                .aktif(request.getAktif() != null ? request.getAktif() : true)
                .build();

        return coaRepository.save(coa);
    }
}