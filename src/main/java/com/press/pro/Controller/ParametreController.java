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

    @PostMapping
    public ResponseEntity<ParametreDto> createParametre(@RequestBody ParametreDto dto) {
        return ResponseEntity.ok(parametreService.createParametre(dto));
    }

    @GetMapping
    public ResponseEntity<List<ParametreDto>> getAllParametres() {
        return ResponseEntity.ok(parametreService.getAllParametres());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParametreDto> getParametreById(@PathVariable Long id) {
        return ResponseEntity.ok(parametreService.getParametreById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ParametreDto> updateParametre(
            @PathVariable Long id,
            @RequestBody ParametreDto dto
    ) {
        return ResponseEntity.ok(parametreService.updateParametre(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteParametre(@PathVariable Long id) {
        parametreService.deleteParametre(id);
        return ResponseEntity.ok("Paramètre supprimé avec succès");
    }
}
