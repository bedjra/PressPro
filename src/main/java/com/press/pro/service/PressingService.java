package com.press.pro.service;

import com.press.pro.Dto.PressingRequest;
import com.press.pro.Entity.Pressing;
import com.press.pro.Entity.Utilisateur;
import com.press.pro.enums.Role;
import com.press.pro.repository.PressingRepository;
import com.press.pro.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PressingService {

    @Autowired
    private PressingRepository pressingRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    // 🔹 Récupération de l'utilisateur connecté
    private Utilisateur getUtilisateurConnecte() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return utilisateurRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new RuntimeException("Utilisateur connecté introuvable"));
    }

    // 🔹 Création d’un pressing et association à l’admin
    @Transactional
    public PressingRequest createPressing(PressingRequest req) {
        Utilisateur user = getUtilisateurConnecte();

        if (user.getPressing() != null) {
            throw new RuntimeException("Vous avez déjà un pressing associé");
        }

        Pressing pressing = new Pressing();
        pressing.setNom(req.getNom());
        pressing.setEmail(user.getEmail()); // Email de l’admin
        pressing.setAdresse(req.getAdresse());
        pressing.setTelephone(req.getTelephone());
        pressing.setCel(req.getCel(pressing.getCel()));
        pressing.setLogo(req.getLogo());

        pressingRepository.save(pressing);

        // ⚡ Associer le pressing à l’utilisateur
        user.setPressing(pressing);
        utilisateurRepository.save(user);

        return mapToDto(pressing);
    }




    // 🔹 Récupérer le pressing de l’utilisateur connecté
    public PressingRequest getPressingPourUtilisateur() {
        Utilisateur user = getUtilisateurConnecte();
        Pressing pressing = user.getPressing();

        if (pressing == null) {
            throw new RuntimeException("Aucun pressing associé");
        }

        return mapToDto(pressing);
    }

    



    // 🔹 Récupérer tous les pressings selon rôle
    public List<PressingRequest> getAllPressings() {
        Utilisateur user = getUtilisateurConnecte();
        List<Pressing> pressings;

        if (user.getRole().equals(Role.ADMIN)) {
            pressings = pressingRepository.findAll();
        } else {
            pressings = user.getPressing() != null ? List.of(user.getPressing()) : List.of();
        }

        return pressings.stream().map(this::mapToDto).toList();
    }

    // 🔹 Mapping Pressing -> DTO
    private PressingRequest mapToDto(Pressing pressing) {
        PressingRequest dto = new PressingRequest();
        dto.setId(pressing.getId());
        dto.setNom(pressing.getNom());
        dto.setAdresse(pressing.getAdresse());
        dto.setTelephone(pressing.getTelephone());
        dto.setLogo(pressing.getLogo());
        dto.getCel(pressing.getCel());
        dto.setEmail(pressing.getEmail());
        return dto;
    }
}
