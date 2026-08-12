package com.belajar.springboot.rc.service;

import com.belajar.springboot.rc.dto.NasabahResponse;
import com.belajar.springboot.rc.dto.RegisterNasabahRequest;
import com.belajar.springboot.rc.dto.UpdateNasabahRequest;
import com.belajar.springboot.rc.entity.Nasabah;
import com.belajar.springboot.rc.entity.NasabahHistory;
import com.belajar.springboot.rc.entity.StatusNasabah;
import com.belajar.springboot.rc.repository.NasabahHistoryRepository;
import com.belajar.springboot.rc.repository.NasabahRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NasabahService {

    @Autowired
    private NasabahRepository nasabahRepository;

    @Autowired
    private NasabahHistoryRepository nasabahHistoryRepository;

    @Transactional
    public NasabahResponse register(RegisterNasabahRequest request, String operatorId) {
        if (nasabahRepository.existsByNik(request.getNik())) {
            throw new RuntimeException("Nasabah dengan NIK " + request.getNik() + " sudah terdaftar di sistem!");
        }

        if (nasabahRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email " + request.getEmail() + " sudah digunakan oleh nasabah lain!");
        }

        String generatedCif = generateCifNumber();

        Nasabah nasabah = Nasabah.builder()
                .cif(generatedCif)
                .nik(request.getNik())
                .namaLengkap(request.getNamaLengkap())
                .tempatLahir(request.getTempatLahir())
                .tanggalLahir(request.getTanggalLahir())
                .jenisKelamin(request.getJenisKelamin())
                .ibuKandung(request.getIbuKandung())
                .alamat(request.getAlamat())
                .nomorHp(request.getNomorHp())
                .email(request.getEmail())
                .pekerjaan(request.getPekerjaan())
                .penghasilanBulanan(request.getPenghasilanBulanan())
                .statusNasabah(StatusNasabah.AKTIF)
                .build();

        nasabahRepository.save(nasabah);

        logHistory(generatedCif, "REGISTER_CIF", "Pendaftaran CIF Nasabah Baru: " + request.getNamaLengkap(), operatorId);

        return mapToResponse(nasabah);
    }

    public List<NasabahResponse> getAllNasabah() {
        return nasabahRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<NasabahResponse> searchNasabah(String keyword) {
        return nasabahRepository.searchNasabah(keyword).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public NasabahResponse getNasabahByCif(String cif) {
        Nasabah nasabah = nasabahRepository.findByCif(cif)
                .orElseThrow(() -> new RuntimeException("Nasabah dengan CIF " + cif + " tidak ditemukan!"));
        return mapToResponse(nasabah);
    }

    @Transactional
    public NasabahResponse updateNasabah(String cif, UpdateNasabahRequest request, String operatorId) {
        Nasabah nasabah = nasabahRepository.findByCif(cif)
                .orElseThrow(() -> new RuntimeException("Nasabah dengan CIF " + cif + " tidak ditemukan!"));

        StringBuilder auditChanges = new StringBuilder("Update CIF Info: ");

        if (!nasabah.getNamaLengkap().equals(request.getNamaLengkap())) {
            auditChanges.append(String.format("[Nama: %s -> %s] ", nasabah.getNamaLengkap(), request.getNamaLengkap()));
            nasabah.setNamaLengkap(request.getNamaLengkap());
        }

        if (!nasabah.getAlamat().equals(request.getAlamat())) {
            auditChanges.append(String.format("[Alamat: %s -> %s] ", nasabah.getAlamat(), request.getAlamat()));
            nasabah.setAlamat(request.getAlamat());
        }

        if (!nasabah.getNomorHp().equals(request.getNomorHp())) {
            auditChanges.append(String.format("[HP: %s -> %s] ", nasabah.getNomorHp(), request.getNomorHp()));
            nasabah.setNomorHp(request.getNomorHp());
        }

        if (!nasabah.getEmail().equals(request.getEmail())) {
            auditChanges.append(String.format("[Email: %s -> %s] ", nasabah.getEmail(), request.getEmail()));
            nasabah.setEmail(request.getEmail());
        }

        if (!nasabah.getPekerjaan().equals(request.getPekerjaan())) {
            auditChanges.append(String.format("[Pekerjaan: %s -> %s] ", nasabah.getPekerjaan(), request.getPekerjaan()));
            nasabah.setPekerjaan(request.getPekerjaan());
        }

        if (nasabah.getPenghasilanBulanan().compareTo(request.getPenghasilanBulanan()) != 0) {
            auditChanges.append(String.format("[Gaji: %s -> %s] ", nasabah.getPenghasilanBulanan(), request.getPenghasilanBulanan()));
            nasabah.setPenghasilanBulanan(request.getPenghasilanBulanan());
        }

        nasabahRepository.save(nasabah);

        logHistory(cif, "UPDATE_PROFILE", auditChanges.toString(), operatorId);

        return mapToResponse(nasabah);
    }

    public List<NasabahHistory> getHistoryByCif(String cif) {
        return nasabahHistoryRepository.findByCifOrderByCreatedAtDesc(cif);
    }

    public List<NasabahHistory> getAllHistory() {
        return nasabahHistoryRepository.findAllByOrderByCreatedAtDesc();
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

    private String generateCifNumber() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = nasabahRepository.count() + 1;
        return String.format("CIF%s%04d", today, count);
    }

    private NasabahResponse mapToResponse(Nasabah nasabah) {
        return NasabahResponse.builder()
                .cif(nasabah.getCif())
                .nik(nasabah.getNik())
                .namaLengkap(nasabah.getNamaLengkap())
                .tempatLahir(nasabah.getTempatLahir())
                .tanggalLahir(nasabah.getTanggalLahir())
                .jenisKelamin(nasabah.getJenisKelamin())
                .ibuKandung(nasabah.getIbuKandung())
                .alamat(nasabah.getAlamat())
                .nomorHp(nasabah.getNomorHp())
                .email(nasabah.getEmail())
                .pekerjaan(nasabah.getPekerjaan())
                .penghasilanBulanan(nasabah.getPenghasilanBulanan())
                .statusNasabah(nasabah.getStatusNasabah())
                .createdAt(nasabah.getCreatedAt())
                .build();
    }
}
