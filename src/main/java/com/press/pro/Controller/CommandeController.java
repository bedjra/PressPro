package com.press.pro.Controller;

import com.press.pro.Dto.DtoCommande;
import com.press.pro.Dto.StatutUpdateRequest;
import com.press.pro.Entity.Commande;
import com.press.pro.enums.StatutCommande;
import com.press.pro.service.CommandeService;
import com.press.pro.service.Pdf.StatutCommandePdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;



@RestController
@RequestMapping("/api/commande")
public class CommandeController {

    @Autowired
    private CommandeService commandeService;

    private final StatutCommandePdfService statutPdfService;

    public CommandeController(CommandeService commandeService,
                              StatutCommandePdfService statutPdfService) {
        this.commandeService = commandeService;
        this.statutPdfService = statutPdfService;
    }


    @PostMapping("/pdf")
    public ResponseEntity<byte[]> createCommandeAvecPdf(@RequestBody DtoCommande dto) {
        return commandeService.saveCommandeEtTelechargerPdf(dto);
    }

    @GetMapping
    public List<DtoCommande> getAllCommandes() {
        return commandeService.getAllCommandes();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DtoCommande> getCommandeById(@PathVariable Long id) {
        try {
            DtoCommande DtoCommande = commandeService.getCommandeById(id);
            return ResponseEntity.ok(DtoCommande);
        } catch (RuntimeException ex) {
            // On peut renvoyer un 404 si commande introuvable ou accès refusé
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }


    //   🔹 Changer le statut d'une commande
    @PostMapping("/{id}/statut")
    public ResponseEntity<byte[]> updateStatutCommande(
            @PathVariable Long id,
            @RequestBody StatutUpdateRequest request) {

        // On ignore request.getNouveauStatut() et on force à LIVREE
        return commandeService.updateStatutCommandeAvecPaiementPdf(
                id,
                request.getMontantActuel() // uniquement le montant
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCommande(@PathVariable("id") Long id) {
        commandeService.deleteCommandeById(id);
        return ResponseEntity.ok("Commande supprimée avec succès !");
    }

    @GetMapping("/annuel")
    public ResponseEntity<Double> getCAAnnuel() {
        Double caAnnuel = commandeService.getCAAnnuel();
        return ResponseEntity.ok(caAnnuel);
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



    @GetMapping("/impayes")
    public Double getTotalImpayes() {
        return commandeService.getTotalImpayes();
    }

    @GetMapping("/totaux")
    public BigDecimal getChiffreAffairesTotal() {
        return commandeService.getChiffreAffairesTotal();
    }



}
