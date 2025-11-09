package com.press.pro.Controller;

import com.press.pro.Dto.PressingRequest;
import com.press.pro.Entity.Pressing;
import com.press.pro.service.PressingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pressing")
public class PressingController {

    @Autowired
    private PressingService pressingService;

    // Création du pressing
    @PostMapping("/create")
    public ResponseEntity<Pressing> createPressing(
            @RequestBody PressingRequest req,
            @RequestHeader("Authorization") String token) {
        Pressing pressing = pressingService.createPressing(req, token);
        return ResponseEntity.ok(pressing);
    }

    // Récupérer le pressing de l’admin connecté
    @GetMapping("/me")
    public ResponseEntity<PressingRequest> getPressingPourUtilisateur(
            @RequestHeader("Authorization") String token
    ) {
        PressingRequest dto = pressingService.getPressingPourUtilisateur(token);
        return ResponseEntity.ok(dto);
    }
    // Mise à jour du pressing
//    @PutMapping("/update/{id}")
//    public ResponseEntity<Pressing> updatePressing(
//            @PathVariable Long id,
//            @RequestBody PressingRequest req,
//            @RequestHeader("Authorization") String token) {
//        Pressing updated = pressingService.updatePressing(id, req, token);
//        return ResponseEntity.ok(updated);
//    }

    @PutMapping("/update/{id}")
    public Pressing updatePressing(@PathVariable Long id,
                                   @RequestBody PressingRequest req,
                                   @RequestHeader("Authorization") String token) {
        return pressingService.updatePressing(id, req, token);
    }


    // Suppression du pressing
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deletePressing(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        pressingService.deletePressing(id, token);
        return ResponseEntity.ok("Pressing supprimé avec succès");
    }
}
