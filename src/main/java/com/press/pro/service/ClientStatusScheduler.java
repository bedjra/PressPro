package com.press.pro.service;

import com.press.pro.Entity.Client;
import com.press.pro.enums.StatutClient;
import com.press.pro.repository.ClientRepository;
import com.press.pro.repository.CommandeRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ClientStatusScheduler {

    private final ClientRepository clientRepository;
    private final CommandeRepository commandeRepository;

    public ClientStatusScheduler(ClientRepository clientRepository, CommandeRepository commandeRepository) {
        this.clientRepository = clientRepository;
        this.commandeRepository = commandeRepository;
    }

    // 🔹 Tous les jours à minuit
    @Scheduled(cron = "0 0 0 * * ?")
    public void updateInactiveClients() {
        LocalDate oneMonthAgo = LocalDate.now().minusMonths(1);

        List<Client> clients = clientRepository.findAll();

        for (Client client : clients) {
            boolean hasCommandesLastMonth = commandeRepository.existsByClientAndDateReceptionAfter(client, oneMonthAgo);

            if (!hasCommandesLastMonth && client.getStatutClient() != StatutClient.Inactif) {
                client.setStatutClient(StatutClient.Inactif);
                clientRepository.save(client);
            }
        }
    }
}
