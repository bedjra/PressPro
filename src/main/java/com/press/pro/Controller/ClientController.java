//package com.press.pro.Controller;
//
//
//import com.press.pro.Entity.Client;
//import com.press.pro.service.ClientService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/client")
//@CrossOrigin(origins = "*")
//@Tag(name = "Clients", description = "CRUD complet pour la gestion des clients")
//public class ClientController {
//
//    private final ClientService clientService;
//
//    public ClientController(ClientService clientService) {
//        this.clientService = clientService;
//    }
//
//    @Operation(summary = "Liste de tous les clients")
//    @GetMapping
//    public List<Client> getAllClients() {
//        return clientService.getAllClients();
//    }
//
//    @Operation(summary = "Récupérer un client par ID")
//    @GetMapping("/{id}")
//    public Client getClientById(@PathVariable Long id) {
//        return clientService.getClientById(id)
//                .orElseThrow(() -> new RuntimeException("Client non trouvé"));
//    }
//
//    @Operation(summary = "Ajouter un nouveau client")
//    @PostMapping
//    public Client addClient(@RequestBody Client client) {
//        return clientService.addClient(client);
//    }
//
//    @Operation(summary = "Modifier un client existant")
//    @PutMapping("/{id}")
//    public Client updateClient(@PathVariable Long id, @RequestBody Client client) {
//        return clientService.updateClient(id, client);
//    }
//
//    @Operation(summary = "Supprimer un client")
//    @DeleteMapping("/{id}")
//    public void deleteClient(@PathVariable Long id) {
//        clientService.deleteClient(id);
//    }
//
//    @Operation(summary = "Rechercher des clients par nom ou téléphone")
//    @GetMapping("/search")
//    public List<Client> searchClients(@RequestParam("keyword") String keyword) {
//        return clientService.searchClients(keyword);
//    }
//
//}
