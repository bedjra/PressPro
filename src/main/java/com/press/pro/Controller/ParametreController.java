//package com.press.pro.Controller;
//
//
//import com.press.pro.Entity.Parametre;
//import com.press.pro.service.ParametreService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.Parameter;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
//import io.swagger.v3.oas.annotations.responses.ApiResponses;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/tarif")
//@CrossOrigin(origins = "*")
//@Tag(name = "Paramètres", description = "CRUD complet pour la gestion des paramètres (articles, services, prix)")
//public class ParametreController {
//
//    private final ParametreService parametreService;
//
//    public ParametreController(ParametreService parametreService) {
//        this.parametreService = parametreService;
//    }
//
//    @Operation(summary = "Lister tous les paramètres", description = "Récupère la liste complète de tous les paramètres disponibles.")
//    @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès")
//    @GetMapping
//    public List<Parametre> getAllParametres() {
//        return parametreService.getAllParametres();
//    }
//
//    @Operation(summary = "Récupérer un paramètre par ID", description = "Renvoie un paramètre spécifique selon son identifiant.")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "Paramètre trouvé"),
//            @ApiResponse(responseCode = "404", description = "Paramètre non trouvé")
//    })
//    @GetMapping("/{id}")
//    public Parametre getParametreById(
//            @Parameter(description = "Identifiant unique du paramètre") @PathVariable Long id) {
//        return parametreService.getParametreById(id)
//                .orElseThrow(() -> new RuntimeException("Paramètre non trouvé"));
//    }
//
//    @Operation(summary = "Ajouter un nouveau paramètre", description = "Crée un nouveau paramètre (article, service, prix).")
//    @ApiResponse(responseCode = "201", description = "Paramètre ajouté avec succès")
//    @PostMapping
//    public Parametre addParametre(
//            @Parameter(description = "Objet Paramètre à enregistrer") @RequestBody Parametre parametre) {
//        return parametreService.addParametre(parametre);
//    }
//
//    @Operation(summary = "Modifier un paramètre existant", description = "Met à jour un paramètre existant en fonction de son ID.")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "Paramètre mis à jour avec succès"),
//            @ApiResponse(responseCode = "404", description = "Paramètre non trouvé")
//    })
//    @PutMapping("/{id}")
//    public Parametre updateParametre(
//            @Parameter(description = "Identifiant du paramètre à modifier") @PathVariable Long id,
//            @Parameter(description = "Nouvelles données du paramètre") @RequestBody Parametre parametre) {
//        return parametreService.updateParametre(id, parametre);
//    }
//
//    @Operation(summary = "Supprimer un paramètre", description = "Supprime un paramètre à partir de son identifiant.")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "204", description = "Paramètre supprimé avec succès"),
//            @ApiResponse(responseCode = "404", description = "Paramètre non trouvé")
//    })
//    @DeleteMapping("/{id}")
//    public void deleteParametre(
//            @Parameter(description = "Identifiant du paramètre à supprimer") @PathVariable Long id) {
//        parametreService.deleteParametre(id);
//    }
//}
