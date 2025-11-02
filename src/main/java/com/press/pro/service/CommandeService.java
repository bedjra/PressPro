package com.press.pro.service;

import com.press.pro.Entity.Commande;
import com.press.pro.repository.CommandeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CommandeService {

    private final CommandeRepository commandeRepository;

    public CommandeService(CommandeRepository commandeRepository) {
        this.commandeRepository = commandeRepository;
    }

    // ✅ Récupérer toutes les commandes
    public List<Commande> getAllCommandes() {
        return commandeRepository.findAll();
    }

    // ✅ Récupérer une commande par ID
    public Optional<Commande> getCommandeById(Long id) {
        return commandeRepository.findById(id);
    }

    // ✅ Ajouter une nouvelle commande
    public Commande addCommande(Commande commande) {
        return commandeRepository.save(commande);
    }

    // ✅ Mettre à jour une commande existante
    public Commande updateCommande(Long id, Commande updatedCommande) {
        return commandeRepository.findById(id)
                .map(existing -> {
                    existing.setClient(updatedCommande.getClient());
                    existing.setParametre(updatedCommande.getParametre());
                    existing.setQte(updatedCommande.getQte());
                    return commandeRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Commande introuvable"));
    }

    // ✅ Supprimer une commande
    public void deleteCommande(Long id) {
        commandeRepository.deleteById(id);
    }
}
