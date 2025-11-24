package com.press.pro.Controller;

import com.press.pro.service.ParametreImportService;
import com.press.pro.service.ResultatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Map;

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

}
