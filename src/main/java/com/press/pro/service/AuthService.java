package com.press.pro.service;

import com.press.pro.Dto.LoginRequest;
import com.press.pro.Dto.RegisterRequest;
import com.press.pro.Entity.Utilisateur;
import com.press.pro.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
}
