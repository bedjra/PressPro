package com.press.pro.Controller;

import com.press.pro.Dto.PressingRequest;
import com.press.pro.Entity.Pressing;
import com.press.pro.service.PressingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Tag(name = "Pressing")
@RestController
@RequestMapping("/api/pressing")
public class PressingController {

    @Autowired
    private PressingService pressingService;

    @Operation(summary = "Créer un pressing et l'associer à l'admin connecté avec un logo")
    @PostMapping("/create")
    @Transactional
    public ResponseEntity<PressingRequest> createPressingWithLogo(
            @RequestBody PressingRequest req // on reçoit directement le DTO en JSON
    ) throws IOException {

        // ⚡ Gestion du logo si c'est un Base64
        String folder = "src/main/resources/static/";
        String logoFilename;

        if (req.getLogo() != null && !req.getLogo().isBlank() && req.getLogo().startsWith("data:image")) {
            String base64Data = req.getLogo().split(",")[1]; // on enlève le prefixe
            byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Data);

            logoFilename = System.currentTimeMillis() + "_logo.png";
            Path path = Paths.get(folder + logoFilename);
            Files.write(path, imageBytes);
        } else if (req.getLogo() == null || req.getLogo().isBlank()) {
            logoFilename = "logo.jpg"; // logo par défaut
        } else {
            // si req.getLogo() contient déjà un nom de fichier existant
            logoFilename = req.getLogo();
        }

        // mettre à jour le DTO pour stocker le nom du fichier
        req.setLogo(logoFilename);

        // créer le pressing
        PressingRequest dto = pressingService.createPressing(req);

        // ⚡ Ajouter l'URL complète du logo pour le frontend
        String baseUrl = "/static/"; // ou "http://localhost:8081/static/" si besoin
        dto.setLogo(baseUrl + dto.getLogo());

        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Récupérer le pressing de l'admin connecté")
    @GetMapping("/me")
    public ResponseEntity<PressingRequest> getPressingPourUtilisateur() {
        PressingRequest dto = pressingService.getPressingPourUtilisateur();
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Mettre à jour le pressing de l'admin connecté")
    @PutMapping("/update/{id}")
    public ResponseEntity<Pressing> updatePressing(
            @PathVariable Long id,
            @RequestBody PressingRequest req) {

        Pressing updated = pressingService.updatePressing(id, req);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Supprimer le pressing de l'admin connecté")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deletePressing(@PathVariable Long id) {
        pressingService.deletePressing(id);
        return ResponseEntity.ok("Pressing supprimé avec succès");
    }
}
