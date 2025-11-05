package com.press.pro.service;

import com.press.pro.Dto.PressingRequest;
import com.press.pro.Entity.Pressing;
import com.press.pro.Entity.Utilisateur;
import com.press.pro.repository.PressingRepository;
import com.press.pro.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PressingService {

    @Autowired
    private PressingRepository pressingRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private JwtService jwtService;

    /**
     * Création d’un pressing et association à l’utilisateur connecté
     */
    public Pressing createPressing(PressingRequest req, String token) {
        String email = jwtService.extractEmail(token.substring(7)); // retirer "Bearer "
        Utilisateur user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (user.getPressing() != null) {
            throw new RuntimeException("Vous avez déjà un pressing associé");
        }

        Pressing pressing = new Pressing();
        pressing.setNom(req.getNom());
        pressing.setEmail(req.getEmail());
        pressing.setTelephone(req.getTelephone());
        pressing.setAdresse(req.getAdresse());
        pressing.setLogo(req.getLogo());

        pressingRepository.save(pressing);

        user.setPressing(pressing);
        utilisateurRepository.save(user);

        return pressing;
    }

    /**
     * Récupérer la liste de tous les pressings
     */
    public List<Pressing> getAllPressings() {
        return pressingRepository.findAll();
    }

    /**
     * Récupérer un pressing par son ID
     */
    public Pressing getPressingById(Long id) {
        return pressingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pressing non trouvé avec l'id : " + id));
    }

    /**
     * Mettre à jour un pressing
     */
    public Pressing updatePressing(Long id, PressingRequest req) {
        Pressing pressing = pressingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pressing non trouvé avec l'id : " + id));

        pressing.setNom(req.getNom());
        pressing.setEmail(req.getEmail());
        pressing.setTelephone(req.getTelephone());
        pressing.setAdresse(req.getAdresse());
        pressing.setLogo(req.getLogo());

        return pressingRepository.save(pressing);
    }

    /**
     * Supprimer un pressing
     */
    public void deletePressing(Long id) {
        Pressing pressing = pressingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pressing non trouvé avec l'id : " + id));

        pressingRepository.delete(pressing);
    }
}
