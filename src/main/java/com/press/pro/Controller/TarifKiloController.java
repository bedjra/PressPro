package com.press.pro.Controller;

import com.press.pro.Dto.TarifKiloDto;
import com.press.pro.service.TarifKiloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kilo")
@CrossOrigin(origins = "*")
public class TarifKiloController {

    @Autowired
    private TarifKiloService tarifKiloService;


    @PostMapping
    public ResponseEntity<TarifKiloDto> ajouterTarif(@RequestBody TarifKiloDto dto) {
        TarifKiloDto saved = tarifKiloService.ajouterTarif(dto);
        return ResponseEntity.ok(saved);
    }


    @GetMapping
    public ResponseEntity<List<TarifKiloDto>> listeTarifs() {
        List<TarifKiloDto> tarifs = tarifKiloService.listeTarifs();
        return ResponseEntity.ok(tarifs);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerTarif(@PathVariable Long id) {
        tarifKiloService.supprimerTarif(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/import")
    public ResponseEntity<String> importDefault() {
        tarifKiloService.importerTarifsParDefaut();
        return ResponseEntity.ok("Importation réussie !");
    }

}
