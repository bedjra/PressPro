package com.press.pro.service;

import com.press.pro.Entity.Charge;
import com.press.pro.Entity.Utilisateur;
import com.press.pro.repository.ChargeRepository;
import com.press.pro.repository.UtilisateurRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ChargeService {

    private final ChargeRepository chargeRepository;
    private final UtilisateurRepository utilisateurRepository;

    public ChargeService(ChargeRepository chargeRepository,
                         UtilisateurRepository utilisateurRepository) {
        this.chargeRepository = chargeRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    // ✅ Récupération du user connecté + pressing automatiquement
    private Utilisateur getUserConnecte() {
        String email = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(auth -> auth.getName())
                .orElseThrow(() -> new RuntimeException("Aucun utilisateur connecté !"));

        Utilisateur user = utilisateurRepository.findDistinctByEmailWithPressing(email.toLowerCase().trim())
                .orElseThrow(() -> new RuntimeException("Utilisateur connecté introuvable : " + email));

        if (user.getPressing() == null)
            throw new RuntimeException("Aucun pressing associé à cet utilisateur !");

        return user;
    }


    // ✅ Création automatique avec le pressing du user
    public Charge create(Charge charge) {

        Utilisateur user = getUserConnecte();

        // Le pressing n’est plus reçu => on l’associe ici
        charge.setPressing(user.getPressing());

        return chargeRepository.save(charge);
    }


    // ✅ Tous les enregistrements appartenant au pressing du user connecté
    public List<Charge> findAll() {
        Utilisateur user = getUserConnecte();
        return chargeRepository.findAll()
                .stream()
                .filter(c -> c.getPressing().getId().equals(user.getPressing().getId()))
                .toList();
    }


    public Optional<Charge> findById(Long id) {
        return chargeRepository.findById(id);
    }


    // ✅ Mise à jour automatique avec le pressing du user
    public Charge update(Long id, Charge updatedCharge) {

        Utilisateur user = getUserConnecte();

        return chargeRepository.findById(id).map(charge -> {
            charge.setDescription(updatedCharge.getDescription());
            charge.setMontant(updatedCharge.getMontant());

            // Le pressing est toujours celui du user
            charge.setPressing(user.getPressing());

            return chargeRepository.save(charge);
        }).orElseThrow(() -> new RuntimeException("Charge introuvable avec l'id : " + id));
    }


    public void delete(Long id) {
        chargeRepository.deleteById(id);
    }
}
