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

    private Utilisateur getUtilisateurConnecte() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return (Utilisateur)this.utilisateurRepository.findByEmail(email.toLowerCase().trim()).orElseThrow(() -> new RuntimeException("Utilisateur connecté introuvable"));
    }

    @Transactional
    public PressingRequest createPressing(PressingRequest req) {
        Utilisateur user = this.getUtilisateurConnecte();
        if (user.getPressing() != null) {
            throw new RuntimeException("Vous avez déjà un pressing associé");
        } else {
            Pressing pressing = new Pressing();
            pressing.setNom(req.getNom());
            pressing.setEmail(user.getEmail());
            pressing.setAdresse(req.getAdresse());
            pressing.setTelephone(req.getTelephone());
            pressing.setCel(req.getCel());
            pressing.setLogo(req.getLogo());
            this.pressingRepository.save(pressing);
            user.setPressing(pressing);
            this.utilisateurRepository.save(user);
            pressing.setActif(true);
            return this.mapToDto(pressing);
        }
    }

    @Transactional
    public PressingRequest updatePressing(PressingRequest req) {
        Utilisateur user = this.getUtilisateurConnecte();
        Pressing pressing = user.getPressing();
        if (pressing == null) {
            throw new RuntimeException("Aucun pressing associé à cet utilisateur");
        } else {
            pressing.setNom(req.getNom());
            pressing.setAdresse(req.getAdresse());
            pressing.setTelephone(req.getTelephone());
            pressing.setCel(req.getCel());
            pressing.setLogo(req.getLogo());
            pressing.setEmail(user.getEmail());
            Pressing updated = (Pressing)this.pressingRepository.save(pressing);
            return this.mapToDto(updated);
        }
    }

    public PressingRequest getPressingPourUtilisateur() {
        Utilisateur user = this.getUtilisateurConnecte();
        Pressing pressing = user.getPressing();
        if (pressing == null) {
            throw new RuntimeException("Aucun pressing associé");
        } else {
            return this.mapToDto(pressing);
        }
    }

    public List<PressingRequest> getAllPressings() {
        Utilisateur user = this.getUtilisateurConnecte();
        List<Pressing> pressings;
        if (user.getRole().equals(Role.ADMIN)) {
            pressings = this.pressingRepository.findAll();
        } else {
            pressings = user.getPressing() != null ? List.of(user.getPressing()) : List.of();
        }

        return pressings.stream().map(this::mapToDto).toList();
    }

    private PressingRequest mapToDto(Pressing pressing) {
        PressingRequest dto = new PressingRequest();
        dto.setId(pressing.getId());
        dto.setNom(pressing.getNom());
        dto.setAdresse(pressing.getAdresse());
        dto.setTelephone(pressing.getTelephone());
        dto.setCel(pressing.getCel());
        dto.setActif(pressing.isActif());
        dto.setDateCreation(pressing.getDateCreation());
        dto.setLogo(pressing.getLogo());
        dto.setEmail(pressing.getEmail());
        return dto;
    }

    @Transactional
    public PressingRequest togglePressingStatus(Long pressingId) {
        Utilisateur user = this.getUtilisateurConnecte();
        if (!user.getRole().equals(Role.ADMIN)) {
            throw new RuntimeException("Accès refusé");
        } else {
            Pressing pressing = (Pressing)this.pressingRepository.findById(pressingId).orElseThrow(() -> new RuntimeException("Pressing introuvable"));
            pressing.setActif(!pressing.isActif());
            return this.mapToDto((Pressing)this.pressingRepository.save(pressing));
        }
    }

    public List<PressingRequest> getAllPressingsFromDB() {
        List<Pressing> pressings = this.pressingRepository.findAll();
        return pressings.stream().map(this::mapToDto).toList();
    }
}
