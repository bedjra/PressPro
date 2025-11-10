package com.press.pro.service;

import com.press.pro.Dto.CommandeDTO;
import com.press.pro.Entity.Client;
import com.press.pro.Entity.Commande;
import com.press.pro.Entity.Parametre;
import com.press.pro.Entity.Utilisateur;
import com.press.pro.enums.StatutCommande;
import com.press.pro.repository.ClientRepository;
import com.press.pro.repository.CommandeRepository;
import com.press.pro.repository.ParametreRepository;
import com.press.pro.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class CommandeService {

    @Autowired
    private CommandeRepository commandeRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ParametreRepository parametreRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private CommandePdfService commandePdfService;

    // 🔹 Méthode utilitaire pour récupérer l'utilisateur connecté avec pressing
    private Utilisateur getUserConnecte() {
        String email = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(auth -> auth.getName())
                .orElseThrow(() -> new RuntimeException("Aucun utilisateur connecté !"));

        Utilisateur user = utilisateurRepository.findDistinctByEmailWithPressing(email.toLowerCase().trim())
                .orElseThrow(() -> new RuntimeException("Utilisateur connecté introuvable : " + email));

        if (user.getPressing() == null)
            throw new RuntimeException("Aucun pressing associé à cet utilisateur !");

        return user;
    }

    // 🔹 Création ou sauvegarde d'une commande
    public CommandeDTO saveCommande(CommandeDTO dto) {
        if (dto.getClientId() == null) throw new RuntimeException("Le client est obligatoire");

        Utilisateur user = getUserConnecte();
        Commande commande = fromDto(dto);
        commande.setPressing(user.getPressing());

        Client client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new RuntimeException("Client introuvable : " + dto.getClientId()));
        commande.setClient(client);

        applyParametreEtMontant(commande, dto.getParametreId());
        applyRemiseEtNet(commande, dto.getRemise());
        applyDates(commande, dto.isExpress(), dto.getDateReception());
        commande.setStatut(StatutCommande.EN_COURS);

        Commande saved = commandeRepository.save(commande);
        commandePdfService.genererCommandePdf(saved); // Génération automatique PDF
        return toDto(saved);
    }

    // 🔹 Création et retour PDF pour téléchargement
    public ResponseEntity<byte[]> saveCommandeEtTelechargerPdf(CommandeDTO dto) {
        CommandeDTO savedDto = saveCommande(dto);
        Commande commande = commandeRepository.findById(savedDto.getId())
                .orElseThrow(() -> new RuntimeException("Commande introuvable : " + savedDto.getId()));
        byte[] pdf = commandePdfService.genererCommandePdf(commande);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=commande_" + commande.getId() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // 🔹 Mise à jour d'une commande
    public CommandeDTO updateCommande(Long id, CommandeDTO dto) {
        Utilisateur user = getUserConnecte();

        Commande commande = commandeRepository.findDistinctByIdWithPressing(id)
                .orElseThrow(() -> new RuntimeException("Commande introuvable : " + id));

        if (!commande.getPressing().getId().equals(user.getPressing().getId()))
            throw new RuntimeException("Accès refusé : cette commande appartient à un autre pressing");

        if (dto.getQte() != null) commande.setQte(dto.getQte());
        if (dto.getParametreId() != null) applyParametreEtMontant(commande, dto.getParametreId());

        applyRemiseEtNet(commande, dto.getRemise() != null ? dto.getRemise() : commande.getRemise());

        if (dto.getDateReception() != null) commande.setDateReception(dto.getDateReception());

        if (dto.isExpress() != commande.isExpress())
            applyDates(commande, dto.isExpress(), commande.getDateReception());

        if (dto.getStatut() != null) commande.setStatut(dto.getStatut());

        return toDto(commandeRepository.save(commande));
    }

    // 🔹 Récupérer toutes les commandes du pressing connecté
    public List<CommandeDTO> getAllCommandes() {
        Utilisateur user = getUserConnecte();
        return commandeRepository.findAllByPressing(user.getPressing())
                .stream().map(this::toDto).toList();
    }

    // 🔹 Génération PDF pour commande existante
    public ResponseEntity<byte[]> telechargerCommandePdf(Long id) {
        Commande commande = commandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande introuvable : " + id));

        byte[] pdf = commandePdfService.genererCommandePdf(commande);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=commande_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // 🔹 Statistiques journalières
    public List<Map<String, Object>> getTotalCommandesParJour() {
        return mapToList(commandeRepository.countCommandesByDay());
    }

    public List<Map<String, Object>> getCommandesEnCoursParJour() {
        return mapToList(commandeRepository.countCommandesByStatutAndDay(StatutCommande.EN_COURS));
    }

    public List<Map<String, Object>> getCommandesLivreesParJour() {
        return mapToList(commandeRepository.countCommandesByStatutAndDay(StatutCommande.LIVREE));
    }

    private List<Map<String, Object>> mapToList(List<Object[]> data) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] obj : data) {
            Map<String, Object> map = new HashMap<>();
            map.put("dateReception", obj[0]);
            map.put("nbCommandes", obj[1]);
            list.add(map);
        }
        return list;
    }

    // 🔹 Conversion DTO -> Entity
    private Commande fromDto(CommandeDTO dto) {
        Commande c = new Commande();
        c.setQte(dto.getQte() != null ? dto.getQte() : 1);
        c.setExpress(dto.isExpress());
        c.setDateReception(dto.getDateReception());
        return c;
    }

    // 🔹 Conversion Entity -> DTO
    public CommandeDTO toDto(Commande c) {
        CommandeDTO dto = new CommandeDTO();
        dto.setId(c.getId());
        if (c.getClient() != null) {
            dto.setClientId(c.getClient().getId());
            dto.setClientNom(c.getClient().getNom());
            dto.setClientTelephone(c.getClient().getTelephone());
        }
        if (c.getParametre() != null) {
            dto.setParametreId(c.getParametre().getId());
            dto.setArticle(c.getParametre().getArticle());
            dto.setService(c.getParametre().getService());
            dto.setPrix(c.getParametre().getPrix());
        }
        dto.setQte(c.getQte());
        dto.setMontantBrut(c.getMontantBrut());
        dto.setRemise(c.getRemise());
        dto.setMontantNet(c.getMontantNet());
        dto.setExpress(c.isExpress());
        dto.setDateReception(c.getDateReception());
        dto.setDateLivraison(c.getDateLivraison());
        dto.setStatut(c.getStatut());
        return dto;
    }

    // 🔹 Méthodes auxiliaires pour simplifier logique
    private void applyParametreEtMontant(Commande c, Long parametreId) {
        Parametre param = parametreRepository.findById(parametreId)
                .orElseThrow(() -> new RuntimeException("Paramètre introuvable : " + parametreId));
        c.setParametre(param);
        c.setMontantBrut(param.getPrix() * c.getQte());
    }

    private void applyRemiseEtNet(Commande c, Double remise) {
        c.setRemise(remise != null ? remise : 0.0);
        c.setMontantNet(c.getMontantBrut() - c.getRemise());
    }

    private void applyDates(Commande c, boolean express, LocalDate dateReception) {
        LocalDate reception = dateReception != null ? dateReception : LocalDate.now();
        c.setDateReception(reception);
        c.setExpress(express);
        c.setDateLivraison(express ? reception.plusDays(1) : reception.plusDays(3));
    }
}
