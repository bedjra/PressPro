package com.press.pro.Controller;

import com.press.pro.Dto.DtoCommande;
import com.press.pro.Dto.DtoCommandeSimple;
import com.press.pro.Dto.StatutUpdateRequest;
import com.press.pro.Entity.Commande;
import com.press.pro.Entity.Utilisateur;
import com.press.pro.enums.StatutCommande;
import com.press.pro.repository.*;
import com.press.pro.service.CommandeService;
import com.press.pro.service.Pdf.CommandePdfService;
import com.press.pro.service.Pdf.StatutCommandePdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;


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

    @Autowired
    private CommandeRepository commandeRepository;


    @Autowired
    private CommandePdfService commandePdfService;


    @Autowired
    private UtilisateurRepository utilisateurRepository;


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



    @PostMapping("/{id}/statut")
    public ResponseEntity<byte[]> updateStatutCommande(
            @PathVariable Long id,
            @RequestBody StatutUpdateRequest request) {

        return commandeService.updateStatutCommandeAvecPaiementPdf(
                id,
                request.getMontantActuel() != null ? request.getMontantActuel() : 0,
                request.getReliquat()   // peut être null → OK
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
    public BigDecimal getCAJournalier() {
        return commandeService.getCAJournalier();
    }

    @GetMapping("/hebdo")
    public Double getCAHebdo() {
        return commandeService.getCAHebdomadaire();
    }


    @GetMapping("/mensuel")
    public ResponseEntity<Double> getCAMensuel() {
        Double caMensuel = commandeService.getCAMensuel(); // utilise sumCAMensuelExact en backend
        return ResponseEntity.ok(caMensuel);
    }



    @GetMapping("/impayes")
    public Double getTotalImpayes() {
        return commandeService.getTotalImpayes();
    }

    // 🔹 Endpoint pour le chiffre d'affaires total
    @GetMapping("/totaux")
    public ResponseEntity<BigDecimal> getChiffreAffaires() {
        BigDecimal chiffreAffaires = commandeService.getChiffreAffairesTotal();
        return ResponseEntity.ok(chiffreAffaires);
    }


    //14 - 02

    @GetMapping("/livree/detail")
    public ResponseEntity<List<DtoCommandeSimple>> getDetailsLivrees() {
        return ResponseEntity.ok(
                commandeService.getDetailsCommandesLivreesParJour()
        );
    }



    @GetMapping("/ca/mensuel")
    public Double getCAMensuel(
            @RequestParam(required = false) Integer mois,
            @RequestParam(required = false) Integer annee
    ) {
        LocalDate now = LocalDate.now();

        int moisFinal = (mois != null) ? mois : now.getMonthValue();
        int anneeFinal = (annee != null) ? annee : now.getYear();

        return commandeService.getCAMensuel(moisFinal, anneeFinal);
    }


    // Récupérer l'utilisateur connecté
    private Utilisateur getUserConnecte() {
        String email = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(auth -> auth.getName())
                .orElseThrow(() -> new RuntimeException("Aucun utilisateur connecté !"));

        return utilisateurRepository.findDistinctByEmailWithPressing(email.toLowerCase().trim())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : " + email));
    }


    @GetMapping("/pdf/{id}")
    public ResponseEntity<byte[]> getCommandePdf(@PathVariable Long id) {

        Commande commande = commandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande introuvable"));

        Utilisateur user = getUserConnecte();

        byte[] pdf = commandePdfService.genererCommandePdf(commande, user);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=commande_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }


}
