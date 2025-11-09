package com.press.pro.Controller;

import com.press.pro.Entity.Commande;
import com.press.pro.service.CommandeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/commandes")
public class CommandeController {

    @Autowired
    private CommandeService commandeService;

    // --- Création d'une commande ---
    @PostMapping("/create")
    public ResponseEntity<Commande> createCommande(@RequestBody Commande commande) {
        try {
            Commande savedCommande = commandeService.createCommande(commande);
            return ResponseEntity.ok(savedCommande);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // --- Mise à jour d'une commande ---
    @PutMapping("/update/{id}")
    public ResponseEntity<Commande> updateCommande(
            @PathVariable Long id,
            @RequestBody Commande updatedCommande
    ) {
        try {
            Commande savedCommande = commandeService.updateCommande(id, updatedCommande);
            return ResponseEntity.ok(savedCommande);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // --- Optionnel : GET d'une commande par ID ---
    @GetMapping("/{id}")
    public ResponseEntity<Commande> getCommande(@PathVariable Long id) {
        try {
            Commande commande = commandeService.updateCommande(id, null); // ou créer un getCommande dans le service
            return ResponseEntity.ok(commande);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
