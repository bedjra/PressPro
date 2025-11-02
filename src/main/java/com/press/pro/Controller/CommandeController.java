package com.press.pro.Controller;


import com.press.pro.Dto.CommandeDTO;
import com.press.pro.Entity.Client;
import com.press.pro.Entity.Commande;
import com.press.pro.Entity.Parametre;
import com.press.pro.repository.ClientRepository;
import com.press.pro.repository.ParametreRepository;
import com.press.pro.service.CommandeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/commandes")
@CrossOrigin(origins = "*")
@Tag(name = "Commandes", description = "Gestion simplifiée des commandes (DTO)")
public class CommandeController {

    private final CommandeService commandeService;
    private final ClientRepository clientRepository;
    private final ParametreRepository parametreRepository;

    public CommandeController(CommandeService commandeService,
                              ClientRepository clientRepository,
                              ParametreRepository parametreRepository) {
        this.commandeService = commandeService;
        this.clientRepository = clientRepository;
        this.parametreRepository = parametreRepository;
    }

    @Operation(summary = "Ajouter une commande simplifiée via DTO")
    @PostMapping("/dto")
    public Commande addCommandeFromDTO(@RequestBody CommandeDTO dto) {

        // Cherche le client existant par nom ou téléphone
        Client client = clientRepository
                .findByTelephoneOrNom(dto.getTelephone(), dto.getNom())
                .orElseThrow(() -> new RuntimeException("Client introuvable"));

        // Cherche l’article par nom et service
        Parametre parametre = parametreRepository
                .findByArticleAndService(dto.getArticle(), dto.getService())
                .orElseThrow(() -> new RuntimeException("Article ou service introuvable"));

        // Crée la commande
        Commande commande = new Commande();
        commande.setClient(client);
        commande.setParametre(parametre);
        commande.setQte(dto.getQte());

        return commandeService.addCommande(commande);
    }
}

