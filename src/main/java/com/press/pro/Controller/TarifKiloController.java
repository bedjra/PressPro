package com.press.pro.Controller;

import com.press.pro.Dto.TarifKiloDto;
import com.press.pro.service.TarifKiloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/kilo")
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

//    @PostMapping("/import")
//    public ResponseEntity<String> importDefault(@RequestParam boolean oui) {
//        if (oui) {
//            tarifKiloService.importerTarifsParDefaut();
//            return ResponseEntity.ok("Importation réussie !");
//        } else {
//            return ResponseEntity.ok("Importation annulée !");
//        }
//    }

    @PostMapping("/import")
    public ResponseEntity<String> importDefault(@RequestBody Map<String, String> body) {
        String choix = body.get("choix");

        if (choix == null) {
            return ResponseEntity.badRequest().body("Le champ 'choix' est manquant.");
        }

        if (!choix.equalsIgnoreCase("oui") && !choix.equalsIgnoreCase("non")) {
            return ResponseEntity.badRequest().body("Valeur invalide. Utilisez 'oui' ou 'non'.");
        }

        if (choix.equalsIgnoreCase("oui")) {
            tarifKiloService.importerTarifsParDefaut();
            return ResponseEntity.ok("Importation réussie !");
        }

        return ResponseEntity.ok("Importation annulée !");
    }




    @GetMapping("/{id}")
    public TarifKiloDto getById(@PathVariable Long id) {
        return tarifKiloService.getById(id);
    }


}
