package com.belajar.springboot.sb.service;

import com.belajar.springboot.gl.service.GlPostingService;
import com.belajar.springboot.sb.dto.BoxBayarRequest;
import com.belajar.springboot.sb.dto.BoxResponse;
import com.belajar.springboot.sb.dto.BoxSewaRequest;
import com.belajar.springboot.sb.entity.SafeDepositBox;
import com.belajar.springboot.sb.entity.StatusBox;
import com.belajar.springboot.sb.repository.SafeDepositBoxRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class SafeDepositBoxService {

    private static final AtomicLong SEQUENCE = new AtomicLong(0);

    @Autowired
    private SafeDepositBoxRepository boxRepository;

    @Autowired
    private GlPostingService glPostingService;

    @Transactional(readOnly = true)
    public List<BoxResponse> inquiry(String status) {
        List<SafeDepositBox> boxes;
        if (status != null && !status.isBlank()) {
            boxes = boxRepository.findByStatusOrderByNomorBoxAsc(StatusBox.valueOf(status.toUpperCase()));
        } else {
            boxes = boxRepository.findAllByOrderByNomorBoxAsc();
        }
        return boxes.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public BoxResponse sewa(BoxSewaRequest request, String operatorId) {
        SafeDepositBox box = boxRepository.findByNomorBox(request.getNomorBox())
                .orElseThrow(() -> new RuntimeException("Box " + request.getNomorBox() + " tidak ditemukan!"));
        if (box.getStatus() != StatusBox.TERSEDIA) {
            throw new RuntimeException("Box " + request.getNomorBox() + " sedang digunakan (TERSEWA)!");
        }

        LocalDate mulai = LocalDate.now();
        LocalDate tempo = mulai.plusMonths(request.getPeriodeBulan());
        BigDecimal tarif = request.getBiayaSewa() != null ? request.getBiayaSewa() : BigDecimal.ZERO;

        box.setStatus(StatusBox.TERSEWA);
        box.setCifPenyewa(request.getCifPenyewa());
        box.setNamaPenyewa(request.getNamaPenyewa());
        box.setNomorRekening(request.getNomorRekening());
        box.setTanggalMulai(mulai);
        box.setTanggalJatuhTempo(tempo);
        box.setTarifSewa(tarif);
        boxRepository.save(box);

        glPostingService.post(GlPostingService.KAS, GlPostingService.PENDAPATAN_JASA, tarif,
                sbRef(request.getNomorBox()), "Sewa box " + request.getNomorBox()
                        + " oleh " + request.getNamaPenyewa() + " (" + request.getPeriodeBulan() + " bulan)",
                operatorId);

        return toResponse(box);
    }

    @Transactional
    public BoxResponse bayarPerpanjang(BoxBayarRequest request, String operatorId) {
        SafeDepositBox box = boxRepository.findByNomorBox(request.getNomorBox())
                .orElseThrow(() -> new RuntimeException("Box " + request.getNomorBox() + " tidak ditemukan!"));
        if (box.getStatus() != StatusBox.TERSEWA) {
            throw new RuntimeException("Box " + request.getNomorBox() + " tidak dalam status TERSEWA!");
        }

        LocalDate tempo = box.getTanggalJatuhTempo() == null
                ? LocalDate.now().plusMonths(request.getPeriodeBulan())
                : box.getTanggalJatuhTempo().plusMonths(request.getPeriodeBulan());
        BigDecimal tarif = request.getBiayaSewa() != null ? request.getBiayaSewa() : BigDecimal.ZERO;

        box.setTanggalJatuhTempo(tempo);
        box.setTarifSewa(tarif);
        boxRepository.save(box);

        glPostingService.post(GlPostingService.KAS, GlPostingService.PENDAPATAN_JASA, tarif,
                sbRef(request.getNomorBox()),
                "Perpanjangan sewa box " + request.getNomorBox()
                        + " (" + request.getPeriodeBulan() + " bulan), tempo menjadi " + tempo,
                operatorId);

        return toResponse(box);
    }

    @Transactional
    public BoxResponse kembalikan(String nomorBox, String operatorId) {
        SafeDepositBox box = boxRepository.findByNomorBox(nomorBox)
                .orElseThrow(() -> new RuntimeException("Box " + nomorBox + " tidak ditemukan!"));
        if (box.getStatus() != StatusBox.TERSEWA) {
            throw new RuntimeException("Box " + nomorBox + " sudah TERSEDIA!");
        }

        box.setStatus(StatusBox.TERSEDIA);
        box.setCifPenyewa(null);
        box.setNamaPenyewa(null);
        box.setNomorRekening(null);
        box.setTanggalMulai(null);
        box.setTanggalJatuhTempo(null);
        box.setTarifSewa(null);
        boxRepository.save(box);

        return toResponse(box);
    }

    private BoxResponse toResponse(SafeDepositBox box) {
        return BoxResponse.builder()
                .nomorBox(box.getNomorBox())
                .status(box.getStatus().name())
                .cifPenyewa(box.getCifPenyewa())
                .namaPenyewa(box.getNamaPenyewa())
                .nomorRekening(box.getNomorRekening())
                .tanggalMulai(box.getTanggalMulai())
                .tanggalJatuhTempo(box.getTanggalJatuhTempo())
                .tarifSewa(box.getTarifSewa())
                .build();
    }

    private String sbRef(String nomorBox) {
        return "SB" + nomorBox.replace("-", "")
                + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
                + String.format("%03d", SEQUENCE.incrementAndGet() % 1000);
    }
}