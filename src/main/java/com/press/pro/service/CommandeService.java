package com.press.pro.service;

import com.press.pro.Entity.Commande;
import com.press.pro.Entity.Client;
import com.press.pro.Entity.Parametre;
import com.press.pro.Entity.Pressing;
import com.press.pro.Entity.Utilisateur;
import com.press.pro.enums.StatutCommande;
import com.press.pro.repository.ClientRepository;
import com.press.pro.repository.CommandeRepository;
import com.press.pro.repository.ParametreRepository;
import com.press.pro.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CommandeService {

    @Autowired
    private CommandeRepository commandeRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ParametreRepository parametreRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    // --- Création d'une commande depuis JSON ---
    public Commande createCommande(Commande commande) {
        if (commande.getClient() == null || commande.getClient().getId() == null) {
            throw new RuntimeException("ClientId manquant dans la commande");
        }
        if (commande.getParametre() == null || commande.getParametre().getId() == null) {
            throw new RuntimeException("ParametreId manquant dans la commande");
        }

        // Récupération du client
        Client client = clientRepository.findById(commande.getClient().getId())
                .orElseThrow(() -> new RuntimeException("Client introuvable"));
        commande.setClient(client);

        // Récupération du paramètre
        Parametre parametre = parametreRepository.findById(commande.getParametre().getId())
                .orElseThrow(() -> new RuntimeException("Paramètre introuvable"));
        commande.setParametre(parametre);

        // 🔹 Récupération du pressing via l'utilisateur connecté
        String emailConnecte = SecurityContextHolder.getContext().getAuthentication().getName();
        Utilisateur utilisateurConnecte = utilisateurRepository.findByEmail(emailConnecte)
                .orElseThrow(() -> new RuntimeException("Utilisateur connecté introuvable"));
        Pressing pressing = utilisateurConnecte.getPressing();
        commande.setPressing(pressing);

        // Statut par défaut
        if (commande.getStatutCommande() == null) {
            commande.setStatutCommande(StatutCommande.EN_COURS);
        }

        return commandeRepository.save(commande);
    }

    // --- Mise à jour d'une commande ---
    public Commande updateCommande(Long id, Commande updatedCommande) {
        Commande existing = commandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande introuvable"));

        existing.setQte(updatedCommande.getQte());
        existing.setExpress(updatedCommande.isExpress());
        existing.setPrixExpress(updatedCommande.getPrixExpress());
        existing.setRemise(updatedCommande.getRemise());
        existing.setStatutCommande(updatedCommande.getStatutCommande());

        // Mise à jour client/parametre depuis le JSON si fourni
        if (updatedCommande.getClient() != null && updatedCommande.getClient().getId() != null) {
            Client client = clientRepository.findById(updatedCommande.getClient().getId())
                    .orElseThrow(() -> new RuntimeException("Client introuvable"));
            existing.setClient(client);
        }
        if (updatedCommande.getParametre() != null && updatedCommande.getParametre().getId() != null) {
            Parametre parametre = parametreRepository.findById(updatedCommande.getParametre().getId())
                    .orElseThrow(() -> new RuntimeException("Paramètre introuvable"));
            existing.setParametre(parametre);
        }

        return commandeRepository.save(existing);
    }
}
