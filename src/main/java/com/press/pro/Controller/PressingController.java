package com.press.pro.Controller;

import com.press.pro.Dto.PressingRequest;
import com.press.pro.Entity.Pressing;
import com.press.pro.service.PressingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pressing")
@Tag(name = "Pressing", description = "Gestion des pressings")
public class PressingController {

    @Autowired
    private PressingService pressingService;



    // ✅ Créer un pressing
    @Operation(summary = "Créer un nouveau pressing et l’associer à un utilisateur")
    @PostMapping
    public ResponseEntity<Pressing> createPressing(
            @RequestBody PressingRequest request,
            @RequestHeader("Authorization") String token) {
        Pressing pressing = pressingService.createPressing(request, token);
        return ResponseEntity.ok(pressing);
    }

    // ✅ Récupérer tous les pressings
    @Operation(summary = "Lister tous les pressings")
    @GetMapping
    public ResponseEntity<List<PressingRequest>> getAllPressings() {
        List<PressingRequest> pressings = pressingService.getAllPressings()
                .stream()
                .map(this::mapToRequest)
                .toList();
        return ResponseEntity.ok(pressings);
    }

    // ✅ Récupérer un pressing par ID
    @Operation(summary = "Obtenir un pressing par son ID")
    @GetMapping("/{id}")
    public ResponseEntity<PressingRequest> getPressingById(@PathVariable Long id) {
        Pressing pressing = pressingService.getPressingById(id);
        return ResponseEntity.ok(mapToRequest(pressing));
    }

    // ✅ Mettre à jour un pressing
    @Operation(summary = "Mettre à jour un pressing existant")
    @PutMapping("/{id}")
    public ResponseEntity<PressingRequest> updatePressing(
            @PathVariable Long id,
            @RequestBody PressingRequest request) {

        Pressing updatedPressing = pressingService.updatePressing(id, request);
        return ResponseEntity.ok(mapToRequest(updatedPressing));
    }

    // ✅ Supprimer un pressing
    @Operation(summary = "Supprimer un pressing par son ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePressing(@PathVariable Long id) {
        pressingService.deletePressing(id);
        return ResponseEntity.ok("Pressing supprimé avec succès !");
    }

    // Méthode de conversion entité → DTO
    private PressingRequest mapToRequest(Pressing pressing) {
        PressingRequest request = new PressingRequest();
        request.setNom(pressing.getNom());
        request.setEmail(pressing.getEmail());
        request.setTelephone(pressing.getTelephone());
        request.setAdresse(pressing.getAdresse());
        request.setLogo(pressing.getLogo());
        return request;
    }
}