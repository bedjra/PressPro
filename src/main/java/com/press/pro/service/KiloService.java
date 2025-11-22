package com.press.pro.service;

import com.press.pro.Dto.KiloDto;
import com.press.pro.Entity.Kilo;
import com.press.pro.Entity.Pressing;
import com.press.pro.Entity.Utilisateur;
import com.press.pro.repository.KiloRepository;
import com.press.pro.repository.UtilisateurRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class KiloService {

    private final KiloRepository kiloRepository;
    private final UtilisateurRepository utilisateurRepository;

    public KiloService(KiloRepository kiloRepository, UtilisateurRepository utilisateurRepository) {
        this.kiloRepository = kiloRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    // 🔹 Récupération de l'utilisateur connecté
    private Utilisateur getUserConnecte() {
        String email = java.util.Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(auth -> auth.getName())
                .orElseThrow(() -> new RuntimeException("Aucun utilisateur connecté !"));

        Utilisateur user = utilisateurRepository.findDistinctByEmailWithPressing(email.toLowerCase().trim())
                .orElseThrow(() -> new RuntimeException("Utilisateur connecté introuvable : " + email));

        if (user.getPressing() == null)
            throw new RuntimeException("Aucun pressing associé à cet utilisateur !");

        return user;
    }

    // Créer un Kilo lié au pressing de l'utilisateur connecté
    public KiloDto createKilo(KiloDto dto) {
        Pressing pressing = getUserConnecte().getPressing();
        Kilo kilo = new Kilo(dto.getPrix(), pressing);
        Kilo saved = kiloRepository.save(kilo);
        return new KiloDto(saved.getId(), saved.getPrix());
    }

    // Lire tous les Kilos du pressing connecté
    public List<KiloDto> getAllKilos() {
        Pressing pressing = getUserConnecte().getPressing();
        return kiloRepository.findAll()
                .stream()
                .filter(k -> k.getPressing().getId().equals(pressing.getId()))
                .map(k -> new KiloDto(k.getId(), k.getPrix()))
                .collect(Collectors.toList());
    }

    // Lire un Kilo par id (vérifie que c'est le pressing de l'utilisateur)
    public KiloDto getKiloById(Long id) {
        Pressing pressing = getUserConnecte().getPressing();
        Kilo kilo = kiloRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kilo introuvable"));
        if (!kilo.getPressing().getId().equals(pressing.getId()))
            throw new RuntimeException("Accès refusé à ce Kilo");
        return new KiloDto(kilo.getId(), kilo.getPrix());
    }

    // Mettre à jour un Kilo
    public KiloDto updateKilo(Long id, KiloDto dto) {
        Pressing pressing = getUserConnecte().getPressing();
        Kilo kilo = kiloRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kilo introuvable"));
        if (!kilo.getPressing().getId().equals(pressing.getId()))
            throw new RuntimeException("Accès refusé à ce Kilo");

        kilo.setPrix(dto.getPrix());
        Kilo updated = kiloRepository.save(kilo);
        return new KiloDto(updated.getId(), updated.getPrix());
    }

    // Supprimer un Kilo
    public void deleteKilo(Long id) {
        Pressing pressing = getUserConnecte().getPressing();
        Kilo kilo = kiloRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kilo introuvable"));
        if (!kilo.getPressing().getId().equals(pressing.getId()))
            throw new RuntimeException("Accès refusé à ce Kilo");

        kiloRepository.delete(kilo);
    }
}
