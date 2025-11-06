package com.press.pro.Controller;

import com.press.pro.Dto.ClientDto;
import com.press.pro.Entity.Utilisateur;
import com.press.pro.repository.UtilisateurRepository;
import com.press.pro.service.ClientService;
import com.press.pro.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/client")
@Tag(name = "Client")

public class ClientController {

    @Autowired
    private ClientService clientService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    private Utilisateur getUserFromToken(String token) {
        String email = jwtService.extractEmail(token.substring(7));
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }

    @Operation(summary = "Créer un client lié automatiquement au pressing de l'utilisateur connecté")
    @PostMapping
    public ResponseEntity<ClientDto> createClient(@RequestBody ClientDto clientDto,
                                                  @RequestHeader("Authorization") String token) {
        Utilisateur user = getUserFromToken(token);
        ClientDto saved = clientService.createClient(clientDto, user);
        return ResponseEntity.ok(saved);
    }

    @Operation(summary = "Lister tous les clients du pressing de l'utilisateur connecté")
    @GetMapping
    public ResponseEntity<List<ClientDto>> getClients(@RequestHeader("Authorization") String token) {
        Utilisateur user = getUserFromToken(token);
        List<ClientDto> clients = clientService.getClients(user);
        return ResponseEntity.ok(clients);
    }

    @Operation(summary = "Récupérer un client par ID")
    @GetMapping("/{id}")
    public ResponseEntity<ClientDto> getClientById(@PathVariable Long id,
                                                   @RequestHeader("Authorization") String token) {
        Utilisateur user = getUserFromToken(token);
        ClientDto client = clientService.getClientById(id, user);
        return ResponseEntity.ok(client);
    }

    @Operation(summary = "Mettre à jour un client")
    @PutMapping("/{id}")
    public ResponseEntity<ClientDto> updateClient(@PathVariable Long id,
                                                  @RequestBody ClientDto updatedClientDto,
                                                  @RequestHeader("Authorization") String token) {
        Utilisateur user = getUserFromToken(token);
        ClientDto client = clientService.updateClient(id, updatedClientDto, user);
        return ResponseEntity.ok(client);
    }

    @Operation(summary = "Supprimer un client")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteClient(@PathVariable Long id,
                                               @RequestHeader("Authorization") String token) {
        Utilisateur user = getUserFromToken(token);
        clientService.deleteClient(id, user);
        return ResponseEntity.ok("Client supprimé avec succès");
    }
}