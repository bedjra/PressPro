package com.press.pro.Controller;

import com.press.pro.Dto.PressingRequest;
import com.press.pro.service.PressingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Pressing")
@RestController
@RequestMapping("/api/pressing")
public class PressingController {

    @Autowired
    private PressingService pressingService;

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



}
