package com.press.pro.Controller;

import com.press.pro.Dto.PressingRequest;
import com.press.pro.Entity.Pressing;
import com.press.pro.service.PressingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    @PostMapping("/logo")
    public String uploadLogo(@RequestParam("file") MultipartFile file) throws IOException {

        String folder = "src/main/resources/static/";
        String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path path = Paths.get(folder + filename);

        Files.copy(file.getInputStream(), path);

        return filename; // tu renvoies juste le nom du fichier
    }


    @Operation(summary = "Créer un pressing et l'associer à l'admin connecté")
    @PostMapping("/create")
    public ResponseEntity<PressingRequest> createPressing(@RequestBody PressingRequest req) {
        PressingRequest dto = pressingService.createPressing(req);
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
