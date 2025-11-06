package com.press.pro.service;

import com.press.pro.Dto.LoginRequest;
import com.press.pro.Dto.RegisterRequest;
import com.press.pro.Entity.Utilisateur;
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


    public String register(RegisterRequest request) {
        if (utilisateurRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email déjà utilisé");
        }

        Utilisateur user = new Utilisateur(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getRole()
        );

//        // ⚡ Lier l’utilisateur au même pressing que le créateur (admin)
//        user.setPressing(creator.getPressing());

        utilisateurRepository.save(user);
        return jwtService.generateToken(user);
    }



    public String login(LoginRequest request) {
        Utilisateur user = utilisateurRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Mot de passe incorrect");
        }

        return jwtService.generateToken(user);
    }



    // service
    public List<RegisterRequest> getAllComptesByAdminConnecte() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Utilisateur connecté introuvable ou non authentifié");
        }
        Object principal = auth.getPrincipal();
        if (!(principal instanceof Utilisateur utilisateurConnecte)) {
            throw new RuntimeException("Utilisateur connecté introuvable ou non authentifié");
        }

        List<Utilisateur> utilisateurs = utilisateurRepository.findByPressing(utilisateurConnecte.getPressing());

        return utilisateurs.stream()
                .map(u -> {
                    RegisterRequest dto = new RegisterRequest();
                    dto.setEmail(u.getEmail());
                    // On masque le mot de passe — on ne révèle pas le hash
                    dto.setPassword("••••••••"); // ou buildMask(u.getPassword())
                    dto.setRole(u.getRole());
                    return dto;
                })
                .toList();
    }




    public Utilisateur updateUser(Long id, RegisterRequest request) {
        // 1️⃣ Récupérer l'utilisateur connecté
        String emailConnecte = SecurityContextHolder.getContext().getAuthentication().getName();
        Utilisateur adminConnecte = utilisateurRepository.findByEmail(emailConnecte)
                .orElseThrow(() -> new RuntimeException("Admin connecté introuvable"));

        // 2️⃣ Trouver l'utilisateur à modifier
        Utilisateur user = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // 3️⃣ Vérifier qu'ils appartiennent au même pressing
        if (!user.getPressing().getId().equals(adminConnecte.getPressing().getId())) {
            throw new RuntimeException("Accès refusé : cet utilisateur n'appartient pas à votre pressing");
        }

        // 4️⃣ Mettre à jour les champs (email, mot de passe, rôle)
        user.setEmail(request.getEmail());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        user.setRole(request.getRole());

        // 5️⃣ Sauvegarder
        return utilisateurRepository.save(user);
    }

}
