package com.press.pro.Controller;

import com.press.pro.Dto.KiloDto;
import com.press.pro.service.KiloService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kilo")
public class KiloController {

    private final KiloService kiloService;

    public KiloController(KiloService kiloService) {
        this.kiloService = kiloService;
    }

    // Créer un Kilo
    @PostMapping
    public ResponseEntity<KiloDto> createKilo(@RequestBody KiloDto dto) {
        return ResponseEntity.ok(kiloService.createKilo(dto));
    }

    // Lire tous les Kilos
    @GetMapping
    public ResponseEntity<List<KiloDto>> getAllKilos() {
        return ResponseEntity.ok(kiloService.getAllKilos());
    }

    // Lire un Kilo par id
    @GetMapping("/{id}")
    public ResponseEntity<KiloDto> getKiloById(@PathVariable Long id) {
        return ResponseEntity.ok(kiloService.getKiloById(id));
    }

    // Mettre à jour
    @PutMapping("/{id}")
    public ResponseEntity<KiloDto> updateKilo(@PathVariable Long id, @RequestBody KiloDto dto) {
        return ResponseEntity.ok(kiloService.updateKilo(id, dto));
    }

    // Supprimer
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteKilo(@PathVariable Long id) {
        kiloService.deleteKilo(id);
        return ResponseEntity.noContent().build();
    }
}
