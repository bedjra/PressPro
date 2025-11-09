package com.press.pro.Controller;

import com.press.pro.Dto.LoginRequest;
import com.press.pro.Dto.PressingRequest;
import com.press.pro.Dto.RegisterRequest;
import com.press.pro.Entity.Utilisateur;
import com.press.pro.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth")

public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/save")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/comptes")
    public ResponseEntity<List<RegisterRequest>> getAllComptesByAdminConnecte() {
        try {
            List<RegisterRequest> comptes = authService.getAllComptesByAdminConnecte();
            return ResponseEntity.ok(comptes);
        } catch (RuntimeException e) {
            // On peut renvoyer un message d'erreur clair
            return ResponseEntity.badRequest().body(null);
        }
    }


    @PutMapping("/{id}")
    public ResponseEntity<Utilisateur> updateUtilisateur(@PathVariable Long id, @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUtilisateur(@PathVariable Long id) {
        authService.deleteUser(id);
        return ResponseEntity.ok("Utilisateur supprimé avec succès");
    }


    // 🌍 Création publique du premier compte (sans être connecté)
    @PostMapping("/public/save")
    public ResponseEntity<String> freeRegister(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.freeRegister(request));
    }


    @GetMapping("/role")
    public ResponseEntity<Map<String, String>> getRole(Authentication authentication) {
        // Récupère le rôle de l'utilisateur connecté
        // Ici on suppose que tu as une méthode pour obtenir le rôle principal
        String role = "USER"; // valeur par défaut
        if (authentication != null && authentication.getAuthorities() != null) {
            role = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse("USER");
        }

        // Retourner un JSON : { "role": "ADMIN" }
        Map<String, String> response = new HashMap<>();
        response.put("role", role);
        return ResponseEntity.ok(response);
    }
}

