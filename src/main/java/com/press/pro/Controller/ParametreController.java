package com.press.pro.Controller;

import com.press.pro.Dto.ParametreDto;
import com.press.pro.service.ParametreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Tag(name = "Tarif")
@RestController
@RequestMapping("/api/parametre")
public class ParametreController {

    @Autowired
    private ParametreService parametreService;

    @Operation(summary = "Créer un nouveau tarif")
    @PostMapping
    public ResponseEntity<ParametreDto> createParametre(@RequestBody ParametreDto dto) {
        return ResponseEntity.ok(parametreService.createParametre(dto));
    }

    @Operation(summary = "Lister tous les tarifs")
    @GetMapping
    public ResponseEntity<List<ParametreDto>> getAllParametres() {
        return ResponseEntity.ok(parametreService.getAllParametres());
    }

    @Operation(summary = "Récupérer un tarif par son ID")
    @GetMapping("/{id}")
    public ResponseEntity<ParametreDto> getParametreById(@PathVariable Long id) {
        return parametreService.getParametreById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Mettre à jour un tarif existant")
    @PutMapping("/{id}")
    public ResponseEntity<ParametreDto> updateParametre(@PathVariable Long id, @RequestBody ParametreDto dto) {
        return parametreService.updateParametre(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Supprimer un tarif par ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteParametre(@PathVariable Long id) {
        boolean deleted = parametreService.deleteParametre(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
