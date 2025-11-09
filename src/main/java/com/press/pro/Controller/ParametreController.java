package com.press.pro.Controller;

import com.press.pro.Dto.ParametreDto;
import com.press.pro.Entity.Utilisateur;
import com.press.pro.service.ParametreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Tarif")
@RestController
@RequestMapping("/api/parametre")
public class ParametreController {

    @Autowired
    private ParametreService parametreService;

    @Operation(summary = "Créer un nouveau tarif pour le pressing connecté")
    @PostMapping
    public ResponseEntity<ParametreDto> createParametre(
            @RequestBody ParametreDto dto,
            @AuthenticationPrincipal Utilisateur utilisateurConnecte
    ) {
        return ResponseEntity.ok(parametreService.createParametre(dto, utilisateurConnecte));
    }

    @Operation(summary = "Lister les tarifs du pressing connecté")
    @GetMapping
    public ResponseEntity<List<ParametreDto>> getAllParametres(
            @AuthenticationPrincipal Utilisateur utilisateurConnecte
    ) {
        return ResponseEntity.ok(parametreService.getAllParametres(utilisateurConnecte));
    }

    @Operation(summary = "Récupérer un tarif par son ID (pressing connecté uniquement)")
    @GetMapping("/{id}")
    public ResponseEntity<ParametreDto> getParametreById(
            @PathVariable Long id,
            @AuthenticationPrincipal Utilisateur utilisateurConnecte
    ) {
        ParametreDto dto = parametreService.getParametreById(id, utilisateurConnecte);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Mettre à jour un tarif existant (pressing connecté uniquement)")
    @PutMapping("/{id}")
    public ResponseEntity<ParametreDto> updateParametre(
            @PathVariable Long id,
            @RequestBody ParametreDto dto,
            @AuthenticationPrincipal Utilisateur utilisateurConnecte
    ) {
        ParametreDto updated = parametreService.updateParametre(id, dto, utilisateurConnecte);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Supprimer un tarif par ID (pressing connecté uniquement)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteParametre(
            @PathVariable Long id,
            @AuthenticationPrincipal Utilisateur utilisateurConnecte
    ) {
        parametreService.deleteParametre(id, utilisateurConnecte);
        return ResponseEntity.noContent().build();
    }
}
