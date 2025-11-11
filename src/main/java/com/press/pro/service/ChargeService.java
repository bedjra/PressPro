package com.press.pro.service;

import com.press.pro.Dto.ChargeDTO;
import com.press.pro.Entity.Charge;
import com.press.pro.Entity.Utilisateur;
import com.press.pro.repository.ChargeRepository;
import com.press.pro.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChargeService {

    private final ChargeRepository chargeRepository;
    private final UtilisateurRepository utilisateurRepository;

    public ChargeService(ChargeRepository chargeRepository,
                         UtilisateurRepository utilisateurRepository) {
        this.chargeRepository = chargeRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    /**
     * Récupère l'utilisateur connecté et son pressing distinct
     */
    private Utilisateur getUserConnecte() {
        String email = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(auth -> auth.getName())
                .orElseThrow(() -> new RuntimeException("Aucun utilisateur connecté !"));

        // 🔹 Utilisation de la méthode distinct pour éviter les doublons Hibernate
        Utilisateur user = utilisateurRepository.findDistinctByEmailWithPressing(email.toLowerCase().trim())
                .orElseThrow(() -> new RuntimeException("Utilisateur connecté introuvable : " + email));

        if (user.getPressing() == null) {
            throw new RuntimeException("Aucun pressing associé à cet utilisateur !");
        }

        return user;
    }

    /**
     * Mapping entity -> DTO
     */
    private ChargeDTO toDTO(Charge charge) {
        return new ChargeDTO(
                charge.getId(),
                charge.getDescription(),
                charge.getMontant(),
                charge.getDateCharge(),
                charge.getPressing().getId(),
                charge.getPressing().getNom()
        );
    }

    /**
     * Création d'une charge
     */
    public ChargeDTO create(Charge charge) {
        Utilisateur user = getUserConnecte();
        charge.setPressing(user.getPressing());
        Charge saved = chargeRepository.save(charge);
        return toDTO(saved);
    }

    /**
     * Récupère toutes les charges du pressing connecté
     */
    public List<ChargeDTO> findAll() {
        Utilisateur user = getUserConnecte();
        return chargeRepository.findAll()
                .stream()
                .filter(c -> c.getPressing() != null &&
                        c.getPressing().getId().equals(user.getPressing().getId()))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupère une charge par son id
     */
    public ChargeDTO findById(Long id) {
        Charge charge = chargeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Charge introuvable avec l'id : " + id));
        return toDTO(charge);
    }

    /**
     * Mise à jour d'une charge
     */
    public ChargeDTO update(Long id, Charge updatedCharge) {
        Utilisateur user = getUserConnecte();

        Charge charge = chargeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Charge introuvable avec l'id : " + id));

        charge.setDescription(updatedCharge.getDescription());
        charge.setMontant(updatedCharge.getMontant());
        charge.setPressing(user.getPressing());

        return toDTO(chargeRepository.save(charge));
    }

    /**
     * Suppression d'une charge
     */
    public void delete(Long id) {
        Charge charge = chargeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Charge introuvable avec l'id : " + id));
        chargeRepository.delete(charge);
    }
}
