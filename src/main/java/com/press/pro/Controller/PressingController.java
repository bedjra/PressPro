package com.press.pro.Controller;


import com.press.pro.Entity.Pressing;
import com.press.pro.service.PressingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pressing")
@Tag(name = "Pressing", description = "CRUD complet pour la gestion des pressings")
public class PressingController {

    private final PressingService pressingService;

    public PressingController(PressingService pressingService) {
        this.pressingService = pressingService;
    }

    @GetMapping
    @Operation(summary = "Lister tous les pressings")
    public List<Pressing> getAllPressings() {
        return pressingService.getAllPressings();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir un pressing par son ID")
    public ResponseEntity<Pressing> getPressingById(@PathVariable Long id) {
        return pressingService.getPressingById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Créer un nouveau pressing")
    public Pressing createPressing(@RequestBody Pressing pressing) {
        return pressingService.createPressing(pressing);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un pressing existant")
    public ResponseEntity<Pressing> updatePressing(@PathVariable Long id, @RequestBody Pressing pressingDetails) {
        return ResponseEntity.ok(pressingService.updatePressing(id, pressingDetails));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un pressing par son ID")
    public ResponseEntity<Void> deletePressing(@PathVariable Long id) {
        pressingService.deletePressing(id);
        return ResponseEntity.noContent().build();
    }
}
