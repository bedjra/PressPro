package com.press.pro.Controller;

import com.press.pro.Entity.Utilisateur;
import com.press.pro.repository.UtilisateurRepository;
import com.press.pro.service.ParametreImportService;
import com.press.pro.service.ResultatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class ResultatController {

    private final ResultatService resultatService;

    public ResultatController(ResultatService resultatService) {
        this.resultatService = resultatService;
    }

    @GetMapping("/net")
    public BigDecimal getResultatNet() {
        return resultatService.getResultatNet();
    }

    @Autowired
    private ParametreImportService parametreImportService;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @PostMapping("/importer")
    public ResponseEntity<?> importer(@RequestBody Map<String, String> body) {

        String choix = body.get("choix");

        if (choix == null) {
            return ResponseEntity.badRequest().body("Le champ 'choix' est manquant.");
        }

        if (!choix.equalsIgnoreCase("oui") && !choix.equalsIgnoreCase("non")) {
            return ResponseEntity.badRequest().body("Valeur invalide. Utilisez 'oui' ou 'non'.");
        }

        if (choix.equalsIgnoreCase("oui")) {
            parametreImportService.importerParametres();
            return ResponseEntity.ok("Importation effectuée avec succès !");
        }

        return ResponseEntity.ok("Importation annulée.");
    }



    @GetMapping("/net/mois")
    public BigDecimal getResultatNetMensuel(
            @RequestParam(required = false) Integer mois
    ) {
        LocalDate now = LocalDate.now();

        int moisFinal = (mois != null) ? mois : now.getMonthValue();
        int anneeFinal = now.getYear();

        return resultatService.getResultatNetMensuel(moisFinal, anneeFinal);
    }



    // Méthode pour récupérer l'utilisateur connecté et son pressing
    private Long getPressingIdUtilisateurConnecte() {
        String email = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(auth -> auth.getName())
                .orElseThrow(() -> new RuntimeException("Aucun utilisateur connecté !"));

        Utilisateur user = utilisateurRepository
                .findDistinctByEmailWithPressing(email.toLowerCase().trim())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : " + email));

        if (user.getPressing() == null) {
            throw new RuntimeException("Aucun pressing associé à cet utilisateur !");
        }

        return user.getPressing().getId();
    }

    // Récupère le CA ou résultat net par mois pour le pressing connecté
    @GetMapping("/graphe")
    public Map<Integer, Map<Integer, BigDecimal>> getResultatNetParMois() {
        Long pressingId = getPressingIdUtilisateurConnecte();
        return resultatService.getResultatNetParMois(pressingId);
    }
}
