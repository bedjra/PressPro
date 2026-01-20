package com.press.pro.service;

import com.press.pro.repository.CommandeRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ResetCAJournalierJob {
    @Autowired
    private CommandeRepository commandeRepository;

    @Scheduled(
            cron = "0 0 0 * * ?"
    )
    @Transactional
    public void resetMontantPayeAujourdHui() {
        this.commandeRepository.resetMontantPayeAujourdHui();
    }

    @Scheduled(
            cron = "0 0 0 * * MON"
    )
    @Transactional
    public void resetMontantPayeSemaine() {
        this.commandeRepository.resetMontantPayeSemaine();
    }
}
