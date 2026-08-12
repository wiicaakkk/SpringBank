package com.belajar.springboot.service;

import com.belajar.springboot.dto.NasabahResponse;
import com.belajar.springboot.dto.RegisterNasabahRequest;
import com.belajar.springboot.dto.UpdateNasabahRequest;
import com.belajar.springboot.entity.Nasabah;
import com.belajar.springboot.entity.NasabahHistory;
import com.belajar.springboot.entity.StatusNasabah;
import com.belajar.springboot.repository.NasabahHistoryRepository;
import com.belajar.springboot.repository.NasabahRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NasabahService {

    private final NasabahRepository nasabahRepository;
    private final NasabahHistoryRepository nasabahHistoryRepository;

    @Autowired
    public NasabahService(NasabahRepository nasabahRepository, NasabahHistoryRepository nasabahHistoryRepository) {
        this.nasabahRepository = nasabahRepository;
        this.nasabahHistoryRepository = nasabahHistoryRepository;
    }

    /**
     * Memproses pendaftaran nasabah bank baru dan membuatkan nomor CIF unik otomatis.
     */
    @Transactional
    public NasabahResponse registerNasabah(RegisterNasabahRequest request) {
        log.info("[CIF REGISTRATION] Memproses pendaftaran nasabah baru untuk NIK: {} (Nama: {})", request.getNik(), request.getNamaLengkap());

        if (nasabahRepository.existsByNik(request.getNik())) {
            log.warn("[CIF REGISTRATION FAILED] NIK {} sudah terdaftar!", request.getNik());
            throw new RuntimeException("NIK " + request.getNik() + " sudah terdaftar di sistem perbankan!");
        }

        if (nasabahRepository.existsByEmail(request.getEmail())) {
            log.warn("[CIF REGISTRATION FAILED] Email {} sudah terdaftar!", request.getEmail());
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

        Nasabah savedNasabah = nasabahRepository.save(nasabah);

        // 📝 SAVE AUDIT TRAIL HISTORY
        saveHistory(generatedCif, "REGISTRATION", "Pendaftaran Nasabah Baru via CIF System. Nama: " + request.getNamaLengkap() + ", NIK: " + request.getNik(), "TELLER_ONLINE");

        log.info("[CIF REGISTRATION SUCCESS] Nasabah berhasil terdaftar dengan Nomor CIF: {}", generatedCif);
        return mapToResponse(savedNasabah);
    }

    public List<NasabahResponse> getAllNasabah() {
        return nasabahRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public NasabahResponse getNasabahByCif(String cif) {
        Nasabah nasabah = nasabahRepository.findByCif(cif)
                .orElseThrow(() -> new RuntimeException("Data Nasabah dengan Nomor CIF " + cif + " tidak ditemukan!"));
        return mapToResponse(nasabah);
    }

    public NasabahResponse getNasabahByNik(String nik) {
        Nasabah nasabah = nasabahRepository.findByNik(nik)
                .orElseThrow(() -> new RuntimeException("Data Nasabah dengan NIK " + nik + " tidak ditemukan!"));
        return mapToResponse(nasabah);
    }

    public List<NasabahResponse> searchNasabah(String keyword) {
        return nasabahRepository.searchNasabah(keyword).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public NasabahResponse updateNasabah(String cif, UpdateNasabahRequest request, String operatorId) {
        Nasabah nasabah = nasabahRepository.findByCif(cif)
                .orElseThrow(() -> new RuntimeException("Data Nasabah dengan Nomor CIF " + cif + " tidak ditemukan!"));

        StringBuilder changes = new StringBuilder("Maintenance Profile CIF: ");
        int changeCount = 0;

        if (request.getNamaLengkap() != null && !request.getNamaLengkap().isBlank() && !request.getNamaLengkap().equals(nasabah.getNamaLengkap())) {
            changes.append("[Nama: '").append(nasabah.getNamaLengkap()).append("' -> '").append(request.getNamaLengkap()).append("'] ");
            nasabah.setNamaLengkap(request.getNamaLengkap());
            changeCount++;
        }
        if (request.getAlamat() != null && !request.getAlamat().isBlank() && !request.getAlamat().equals(nasabah.getAlamat())) {
            changes.append("[Alamat: '").append(nasabah.getAlamat()).append("' -> '").append(request.getAlamat()).append("'] ");
            nasabah.setAlamat(request.getAlamat());
            changeCount++;
        }
        if (request.getNomorHp() != null && !request.getNomorHp().isBlank() && !request.getNomorHp().equals(nasabah.getNomorHp())) {
            changes.append("[No HP: '").append(nasabah.getNomorHp()).append("' -> '").append(request.getNomorHp()).append("'] ");
            nasabah.setNomorHp(request.getNomorHp());
            changeCount++;
        }
        if (request.getEmail() != null && !request.getEmail().isBlank() && !request.getEmail().equals(nasabah.getEmail())) {
            changes.append("[Email: '").append(nasabah.getEmail()).append("' -> '").append(request.getEmail()).append("'] ");
            nasabah.setEmail(request.getEmail());
            changeCount++;
        }
        if (request.getPekerjaan() != null && !request.getPekerjaan().isBlank() && !request.getPekerjaan().equals(nasabah.getPekerjaan())) {
            changes.append("[Pekerjaan: '").append(nasabah.getPekerjaan()).append("' -> '").append(request.getPekerjaan()).append("'] ");
            nasabah.setPekerjaan(request.getPekerjaan());
            changeCount++;
        }
        if (request.getPenghasilanBulanan() != null && request.getPenghasilanBulanan().compareTo(nasabah.getPenghasilanBulanan()) != 0) {
            changes.append("[Penghasilan: '").append(nasabah.getPenghasilanBulanan()).append("' -> '").append(request.getPenghasilanBulanan()).append("'] ");
            nasabah.setPenghasilanBulanan(request.getPenghasilanBulanan());
            changeCount++;
        }

        if (changeCount == 0) {
            changes.append("Tidak ada atribut profil yang berubah.");
        }

        Nasabah updated = nasabahRepository.save(nasabah);

        String user = (operatorId != null && !operatorId.isBlank()) ? operatorId : "SYSTEM_USER";
        saveHistory(cif, "UPDATE_PROFILE", changes.toString(), user);

        return mapToResponse(updated);
    }

    @Transactional
    public NasabahResponse updateStatusNasabah(String cif, StatusNasabah statusBaru) {
        Nasabah nasabah = nasabahRepository.findByCif(cif)
                .orElseThrow(() -> new RuntimeException("Data Nasabah dengan Nomor CIF " + cif + " tidak ditemukan!"));

        StatusNasabah statusLama = nasabah.getStatusNasabah();
        nasabah.setStatusNasabah(statusBaru);

        Nasabah updated = nasabahRepository.save(nasabah);

        // 📝 SAVE AUDIT TRAIL HISTORY
        saveHistory(cif, "STATUS_CHANGE", "Perubahan Status Nasabah: " + statusLama + " -> " + statusBaru, "SUPERVISOR_BANK");

        return mapToResponse(updated);
    }

    /**
     * Mengambil riwayat Audit Trail perubahan nasabah berdasarkan CIF.
     */
    public List<NasabahHistory> getNasabahHistory(String cif) {
        return nasabahHistoryRepository.findByCifOrderByCreatedAtDesc(cif);
    }

    /**
     * Mengambil seluruh Audit Trail nasabah di sistem perbankan.
     */
    public List<NasabahHistory> getAllHistory() {
        return nasabahHistoryRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Private helper untuk menyimpan catatan history ke database.
     */
    private void saveHistory(String cif, String actionType, String keterangan, String updatedBy) {
        NasabahHistory history = NasabahHistory.builder()
                .cif(cif)
                .actionType(actionType)
                .keterangan(keterangan)
                .updatedBy(updatedBy)
                .build();
        nasabahHistoryRepository.save(history);
    }

    private String generateCifNumber() {
        String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long nextSequence = nasabahRepository.countTotalNasabah() + 1;
        return String.format("CIF%s%04d", datePrefix, nextSequence);
    }

    private NasabahResponse mapToResponse(Nasabah nasabah) {
        return NasabahResponse.builder()
                .id(nasabah.getId())
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
