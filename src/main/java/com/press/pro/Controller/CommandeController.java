package com.press.pro.Controller;

import com.press.pro.Dto.CommandeDTO;
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

    // 🔹 2️⃣ Télécharger un PDF existant à partir de l'ID de la commande
//    @GetMapping("/commande/pdf/last/{idCommande}")
//    public ResponseEntity<Resource> getLastPdf(@PathVariable Long idCommande) throws IOException {
//        Path dossier = Paths.get(DOSSIER_COMMANDES);
//        if (!Files.exists(dossier)) {
//            return ResponseEntity.notFound().build();
//        }
//
//        // Filtrer les fichiers qui correspondent à la commande
//        Optional<Path> dernierPdf = Files.list(dossier)
//                .filter(f -> f.getFileName().toString().startsWith("Commande_" + idCommande))
//                .max(Comparator.comparingLong(f -> f.toFile().lastModified())); // le plus récent
//
//        if (dernierPdf.isEmpty()) {
//            return ResponseEntity.notFound().build();
//        }
//
//        Path fichier = dernierPdf.get();
//        Resource resource = new UrlResource(fichier.toUri());
//
//        return ResponseEntity.ok()
//                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fichier.getFileName() + "\"")
//                .contentType(MediaType.APPLICATION_PDF)
//                .body(resource);
//    }


}
