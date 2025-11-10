package com.press.pro.Controller;

import com.press.pro.Dto.CommandeDTO;
import com.press.pro.service.CommandeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
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


    @PutMapping("/{id}")
    public CommandeDTO updateCommande(@PathVariable Long id, @RequestBody CommandeDTO commandeDTO) {
        return commandeService.updateCommande(id, commandeDTO);
    }


    // 1️⃣ Toutes les commandes par jour
    @GetMapping("/total")
    public List<Map<String, Object>> getTotalCommandesParJour() {
        return commandeService.getTotalCommandesParJour();
    }

    // 2️⃣ Commandes EN_COURS par jour
    @GetMapping("/cours")
    public List<Map<String, Object>> getCommandesEnCoursParJour() {
        return commandeService.getCommandesEnCoursParJour();
    }

    // 3️⃣ Commandes LIVREE par jour
    @GetMapping("/livree")
    public List<Map<String, Object>> getCommandesLivreesParJour() {
        return commandeService.getCommandesLivreesParJour();
    }
}
