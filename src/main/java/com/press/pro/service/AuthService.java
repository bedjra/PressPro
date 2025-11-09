package com.press.pro.service;

import com.press.pro.Dto.LoginRequest;
import com.press.pro.Dto.PressingRequest;
import com.press.pro.Dto.RegisterRequest;
import com.press.pro.Entity.Pressing;
import com.press.pro.Entity.Utilisateur;
import com.press.pro.enums.Role;
import com.press.pro.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    // 🔹 Enregistrement d’un nouvel utilisateur
    public String register(RegisterRequest request) {
        String emailNormalized = request.getEmail().toLowerCase().trim();

        if (utilisateurRepository.findByEmail(emailNormalized).isPresent()) {
            throw new RuntimeException("Email déjà utilisé");
        }

        // 🔹 Récupérer l'utilisateur connecté
        String emailConnecte = SecurityContextHolder.getContext().getAuthentication().getName();
        Utilisateur userConnecte = utilisateurRepository.findByEmail(emailConnecte.toLowerCase().trim())
                .orElseThrow(() -> new RuntimeException("Utilisateur connecté introuvable"));

        // 🔹 Seul l'admin peut créer d'autres comptes
        if (!userConnecte.getRole().equals(Role.ADMIN)) {
            throw new RuntimeException("Seul un admin peut créer de nouveaux comptes");
        }

        // 🔹 Créer le nouvel utilisateur avec le rôle fourni
        Utilisateur user = new Utilisateur(
                emailNormalized,
                passwordEncoder.encode(request.getPassword()),
                request.getRole()
        );

        // 🔗 Lier le compte au pressing de l’admin
        user.setPressing(userConnecte.getPressing());

        utilisateurRepository.save(user);
        return jwtService.generateToken(user);
    }


    // 🌍 Création libre d’un compte utilisateur (sans lien à un pressing, sans contrôle d’admin)
    public String freeRegister(RegisterRequest request) {
        // 🔹 Normaliser l'email
        String emailNormalized = request.getEmail().toLowerCase().trim();

        // 🔹 Vérifier si l’email existe déjà
        if (utilisateurRepository.findByEmail(emailNormalized).isPresent()) {
            throw new RuntimeException("Email déjà utilisé");
        }

        // 🔹 Créer le nouvel utilisateur
        Utilisateur user = new Utilisateur(
                emailNormalized,
                passwordEncoder.encode(request.getPassword()),
                request.getRole() // ex : ADMIN, USER, etc.
        );

        // 🔹 Aucun lien à un pressing ici
        user.setPressing(null);

        // 🔹 Enregistrer le compte
        utilisateurRepository.save(user);

        // 🔹 Générer un token JWT pour le nouvel utilisateur (facultatif)
        return jwtService.generateToken(user);
    }


    // 🔹 Connexion
    public String login(LoginRequest request) {
        String emailNormalized = request.getEmail().toLowerCase().trim();

        Utilisateur user = utilisateurRepository.findByEmail(emailNormalized)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Mot de passe incorrect");
        }

        return jwtService.generateToken(user);
    }


    public List<RegisterRequest> getAllComptesByAdminConnecte() {
        // 1️⃣ Récupération de l'utilisateur connecté
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new RuntimeException("Aucun utilisateur connecté !");
        }

        String emailConnecte = auth.getName().toLowerCase().trim();

        // 2️⃣ Récupération de l'utilisateur unique avec son pressing pour éviter les doublons Hibernate
        Utilisateur userConnecte = utilisateurRepository
                .findDistinctByEmailWithPressing(emailConnecte)
                .orElseThrow(() -> new RuntimeException("Utilisateur connecté introuvable : " + emailConnecte));

        // 3️⃣ Récupération des comptes selon le rôle
        List<Utilisateur> utilisateurs;
        if (userConnecte.getRole() == Role.ADMIN) {
            if (userConnecte.getPressing() == null) {
                throw new RuntimeException("Pressing non défini pour l'admin !");
            }
            utilisateurs = utilisateurRepository.findAllByPressing(userConnecte.getPressing());
        } else {
            utilisateurs = List.of(userConnecte);
        }

        // 4️⃣ Transformation en DTO
        return utilisateurs.stream()
                .map(u -> {
                    RegisterRequest dto = new RegisterRequest();
                    dto.setId(u.getId());
                    dto.setEmail(u.getEmail());
                    dto.setPassword("•••••"); // ne jamais exposer le mot de passe réel
                    dto.setRole(u.getRole());
                    return dto;
                })
                .toList();
    }


    // 🔹 Mise à jour d’un utilisateur
    public Utilisateur updateUser(Long id, RegisterRequest request) {
        String emailConnecte = SecurityContextHolder.getContext().getAuthentication().getName();
        Utilisateur adminConnecte = utilisateurRepository.findByEmail(emailConnecte.toLowerCase().trim())
                .orElseThrow(() -> new RuntimeException("Admin connecté introuvable"));

        Utilisateur user = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        if (!user.getPressing().getId().equals(adminConnecte.getPressing().getId())) {
            throw new RuntimeException("Accès refusé : cet utilisateur n'appartient pas à votre pressing");
        }

        // ✅ Mise à jour des champs
        user.setEmail(request.getEmail().toLowerCase().trim());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        user.setRole(request.getRole());

        return utilisateurRepository.save(user);
    }

    // 🔹 Suppression d’un utilisateur
    public void deleteUser(Long id) {
        String emailConnecte = SecurityContextHolder.getContext().getAuthentication().getName();
        Utilisateur adminConnecte = utilisateurRepository.findByEmail(emailConnecte.toLowerCase().trim())
                .orElseThrow(() -> new RuntimeException("Admin connecté introuvable"));

        Utilisateur user = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        if (!user.getPressing().getId().equals(adminConnecte.getPressing().getId())) {
            throw new RuntimeException("Accès refusé : cet utilisateur n'appartient pas à votre pressing");
        }

        utilisateurRepository.delete(user);
    }
}
