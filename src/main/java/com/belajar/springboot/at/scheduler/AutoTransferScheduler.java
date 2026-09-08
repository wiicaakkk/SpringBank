package com.belajar.springboot.at.scheduler;

import com.belajar.springboot.at.dto.ExecuteAutoTransferResponse;
import com.belajar.springboot.at.service.AutoTransferService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AutoTransferScheduler {

    private static final Logger log = LoggerFactory.getLogger(AutoTransferScheduler.class);

    @Autowired
    private AutoTransferService autoTransferService;

    @Scheduled(cron = "0 15 7 * * *", zone = "Asia/Jakarta")
    public void eksekusiHarian() {
        ExecuteAutoTransferResponse hasil = autoTransferService.execute(null, "SYSTEM");
        log.info("[AUTO_TRANSFER] Eksekusi harian: {} terproses, {} sukses, {} gagal",
                hasil.getTerproses(), hasil.getSukses(), hasil.getGagal());
    }
}