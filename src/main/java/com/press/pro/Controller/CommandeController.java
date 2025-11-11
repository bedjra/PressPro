package com.press.pro.Controller;

import com.press.pro.Dto.CommandeDTO;
import com.press.pro.enums.StatutCommande;
import com.press.pro.service.CommandeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
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
            CommandeDTO dto = commandeService.getCommandeById(id);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(404)
                    .body(null); // ou tu peux renvoyer un message d'erreur personnalisé
        }
    }



    @PutMapping("/{id}")
    public CommandeDTO updateCommande(@PathVariable Long id, @RequestBody CommandeDTO commandeDTO) {
        return commandeService.updateCommande(id, commandeDTO);
    }


    @GetMapping("/total")
    public List<Map<String, Object>> getTotalCommandesParJour() {
        return commandeService.getTotalCommandesParJour();
    }

    @GetMapping("/cours")
    public List<Map<String, Object>> getCommandesEnCoursParJour() {
        return commandeService.getCommandesEnCoursParJour();
    }

    @GetMapping("/livree")
    public List<Map<String, Object>> getCommandesLivreesParJour() {
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
    @PutMapping("/{id}/statut")
    public CommandeDTO changerStatut(
            @PathVariable("id") Long commandeId,
            @RequestParam("statut") StatutCommande statut) {

        return commandeService.updateStatutCommande(commandeId, statut);
    }
}
