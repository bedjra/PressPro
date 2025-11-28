package com.press.pro.service;

import com.press.pro.Entity.Utilisateur;
import com.press.pro.repository.*;
import com.press.pro.service.Pdf.CommandePdfService;
import com.press.pro.service.Pdf.StatutCommandePdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CommadeServiceNew {

    @Autowired
    private CommandeRepository commandeRepository;

    @Autowired
    private CommandeLigneRepository commandeLigneRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ParametreRepository parametreRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private CommandePdfService commandePdfService;

    @Autowired
    private StatutCommandePdfService statutCommandePdfService;


    // Récupérer l'utilisateur connecté
    private Utilisateur getUserConnecte() {
        String email = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(auth -> auth.getName())
                .orElseThrow(() -> new RuntimeException("Aucun utilisateur connecté !"));

        return utilisateurRepository.findDistinctByEmailWithPressing(email.toLowerCase().trim())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : " + email));
    }



}
