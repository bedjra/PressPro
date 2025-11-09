package com.press.pro.service;

import com.press.pro.Dto.PressingRequest;
import com.press.pro.Entity.Pressing;
import com.press.pro.Entity.Utilisateur;
import com.press.pro.repository.PressingRepository;
import com.press.pro.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PressingService {

    @Autowired
    private PressingRepository pressingRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private JwtService jwtService;

    // Création d’un pressing et association à l’admin
    public Pressing createPressing(PressingRequest req, String token) {
        String email = jwtService.extractEmail(token.substring(7));
        Utilisateur user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (user.getPressing() != null) {
            throw new RuntimeException("Vous avez déjà un pressing associé");
        }

        Pressing pressing = new Pressing();
        pressing.setNom(req.getNom());
        pressing.setEmail(user.getEmail());   // Email de l'admin
        pressing.setTelephone(req.getTelephone());
        pressing.setAdresse(req.getAdresse());
        pressing.setLogo(req.getLogo());      // URL du logo

        pressingRepository.save(pressing);

        user.setPressing(pressing);
        utilisateurRepository.save(user);

        return pressing;
    }



    public PressingRequest getPressingPourUtilisateur(String token) {
        String email = jwtService.extractEmail(token.substring(7));
        Utilisateur user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Pressing pressing = user.getPressing();
        if (pressing == null) {
            throw new RuntimeException("Aucun pressing associé");
        }

        // Convertir l'entité en DTO
        PressingRequest dto = new PressingRequest();
        dto.setId(pressing.getId());
        dto.setNom(pressing.getNom());
        dto.setAdresse(pressing.getAdresse());
        dto.setTelephone(pressing.getTelephone());
        dto.setLogo(pressing.getLogo() != null ? new String(pressing.getLogo()) : null); // convertir bytes en String si nécessaire
        dto.setEmail(pressing.getEmail());

        return dto;
    }



    // Mise à jour d’un pressing (seul l’admin propriétaire peut modifier)
    public Pressing updatePressing(Long id, PressingRequest req, String token) {
        // 1️⃣ On récupère le pressing existant
        Pressing pressing = pressingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pressing non trouvé"));

        // 2️⃣ Vérification que le pressing appartient bien à l’utilisateur
        String email = jwtService.extractEmail(token.substring(7));
        if (!pressing.getEmail().equals(email)) {
            throw new RuntimeException("Vous n’êtes pas autorisé à modifier ce pressing");
        }

        // 3️⃣ Mise à jour des champs modifiables
        pressing.setNom(req.getNom());
        pressing.setTelephone(req.getTelephone());
        pressing.setAdresse(req.getAdresse());
        pressing.setLogo(req.getLogo());

        // 4️⃣ On sauvegarde sans toucher à l’id
        return pressingRepository.save(pressing);
    }


    // Suppression d’un pressing (seul l’admin propriétaire peut supprimer)
    public void deletePressing(Long id, String token) {
        Pressing pressing = pressingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pressing non trouvé"));

        String email = jwtService.extractEmail(token.substring(7));
        if (!pressing.getEmail().equals(email)) {
            throw new RuntimeException("Vous n’êtes pas autorisé à supprimer ce pressing");
        }

        pressingRepository.delete(pressing);
    }
}
