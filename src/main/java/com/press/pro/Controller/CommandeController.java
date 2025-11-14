package com.press.pro.Controller;

import com.press.pro.Dto.CommandeDTO;
import com.press.pro.enums.StatutCommande;
import com.press.pro.service.CommandeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;



@RestController
@RequestMapping("/api/commande")
public class CommandeController {

    @Autowired
    private CommandeService commandeService;


    @PostMapping
    public CommandeDTO saveCommande(@RequestBody CommandeDTO commandeDTO) {
        return commandeService.saveCommande(commandeDTO);
    }

    @PostMapping("/pdf")
    public ResponseEntity<byte[]> createCommandeAvecPdf(@RequestBody CommandeDTO dto) {
        return commandeService.saveCommandeEtTelechargerPdf(dto);
    }


    @GetMapping
    public List<CommandeDTO> getAllCommandes() {
        return commandeService.getAllCommandes();
    }



    @GetMapping("/{id}")
    public ResponseEntity<CommandeDTO> getCommandeById(@PathVariable Long id) {
        try {
            CommandeDTO commandeDTO = commandeService.getCommandeById(id);
            return ResponseEntity.ok(commandeDTO);
        } catch (RuntimeException ex) {
            // On peut renvoyer un 404 si commande introuvable ou accès refusé
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }


    @PutMapping("/{id}")
    public CommandeDTO updateCommande(@PathVariable Long id, @RequestBody CommandeDTO commandeDTO) {
        return commandeService.updateCommande(id, commandeDTO);
    }


    @GetMapping("/total")
    public Map<String, Object> getTotalCommandesParJour() {
        return commandeService.getTotalCommandesParJour();
    }

    @GetMapping("/cours")
    public Map<String, Object> getCommandesEnCoursParJour() {
        return commandeService.getCommandesEnCoursParJour();
    }

    @GetMapping("/livree")
    public Map<String, Object> getCommandesLivreesParJour() {
        return commandeService.getCommandesLivreesParJour();
    }


    @GetMapping("/jour")
    public Double getCAJournalier() {
        return commandeService.getCAJournalier();
    }

    @GetMapping("/hebdo")
    public Double getCAHebdo() {
        return commandeService.getCAHebdomadaire();
    }

    @GetMapping("/mensuel")
    public Double getCAMensuel() {
        return commandeService.getCAMensuel();
    }

    @GetMapping("/annuel")
    public Double getCAAnnuel() {
        return commandeService.getCAAnnuel();
    }

    @GetMapping("/impayes")
    public Double getTotalImpayes() {
        return commandeService.getTotalImpayes();
    }

    // 🔹 Changer le statut d'une commande
//    @PutMapping("/{id}/statut")
//    public CommandeDTO changerStatut(
//            @PathVariable("id") Long commandeId,
//            @RequestParam("statut") StatutCommande statut) {
//
//        return commandeService.updateStatutCommande(commandeId, statut);
//    }

    @PostMapping("/{id}/statut")
    public CommandeDTO updateStatutAvecPaiement(
            @PathVariable Long commandeId,
            @RequestBody StatutPaiementRequest request) {

        return commandeService.updateStatutCommandeAvecPaiement(
                commandeId,
                request.getStatut(),
                request.getMontantActuel()
        );
    }

    // --- DTO pour la requête ---
    public static class StatutPaiementRequest {
        private StatutCommande statut;
        private double montantActuel;

        public StatutCommande getStatut() { return statut; }
        public void setStatut(StatutCommande statut) { this.statut = statut; }

        public double getMontantActuel() { return montantActuel; }
        public void setMontantActuel(double montantActuel) { this.montantActuel = montantActuel; }
    }

    @GetMapping("/totaux")
    public BigDecimal getChiffreAffairesTotal() {
        return commandeService.getChiffreAffairesTotal();
    }


}
