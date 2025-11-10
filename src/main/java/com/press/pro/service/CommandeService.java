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



    // 🔹 Nouvelle méthode : crée la commande ET renvoie le PDF à télécharger
    public ResponseEntity<byte[]> saveCommandeEtTelechargerPdf(CommandeDTO commandeDTO) {
        // 1️⃣ Création normale de la commande
        Commande commande = fromDto(commandeDTO);

        if (commandeDTO.getClientId() == null) {
            throw new RuntimeException("Le client est obligatoire pour créer une commande");
        }

        // --- récupération de l’utilisateur connecté
        String emailConnecte = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(auth -> auth.getName())
                .orElseThrow(() -> new RuntimeException("Aucun utilisateur connecté !"));

        Utilisateur userConnecte = utilisateurRepository
                .findDistinctByEmailWithPressing(emailConnecte.toLowerCase().trim())
                .orElseThrow(() -> new RuntimeException("Utilisateur connecté introuvable : " + emailConnecte));

        if (userConnecte.getPressing() == null)
            throw new RuntimeException("Aucun pressing associé à cet utilisateur !");

        commande.setPressing(userConnecte.getPressing());

        // --- association du client
        Client client = clientRepository.findById(commandeDTO.getClientId())
                .orElseThrow(() -> new RuntimeException("Client introuvable avec l'ID : " + commandeDTO.getClientId()));
        commande.setClient(client);

        // --- association du paramètre
        if (commandeDTO.getParametreId() != null) {
            Parametre parametre = parametreRepository.findById(commandeDTO.getParametreId())
                    .orElseThrow(() -> new RuntimeException("Paramètre introuvable avec l'ID : " + commandeDTO.getParametreId()));
            commande.setParametre(parametre);
            double montantBrutCalcule = parametre.getPrix() * commande.getQte();
            commande.setMontantBrut(montantBrutCalcule);
        } else {
            commande.setMontantBrut(0.0);
        }

        // --- calcul montant net
        double remise = commandeDTO.getRemise() != null ? commandeDTO.getRemise() : 0.0;
        commande.setRemise(remise);
        commande.setMontantNet(commande.getMontantBrut() - remise);

        // --- gestion des dates
        LocalDate dateReception = Optional.ofNullable(commandeDTO.getDateReception()).orElse(LocalDate.now());
        commande.setDateReception(dateReception);
        LocalDate dateLivraison = commandeDTO.isExpress()
                ? dateReception.plusDays(1)
                : dateReception.plusDays(3);
        commande.setDateLivraison(dateLivraison);
        commande.setExpress(commandeDTO.isExpress());
        commande.setStatut(StatutCommande.EN_COURS);

        // --- sauvegarde
        Commande saved = commandeRepository.save(commande);

        // 2️⃣ Génération du PDF en mémoire
        byte[] pdfBytes = commandePdfService.genererCommandePdf(saved);

        // 3️⃣ Retour de la réponse téléchargeable
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=commande_" + saved.getId() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    // Création d'une commande avec génération automatique du PDF
    public CommandeDTO saveCommande(CommandeDTO commandeDTO) {

        if (commandeDTO.getClientId() == null) {
            throw new RuntimeException("Le client est obligatoire pour créer une commande");
        }

        Commande commande = fromDto(commandeDTO);

        // Récupération utilisateur connecté
        String emailConnecte = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(auth -> auth.getName())
                .orElseThrow(() -> new RuntimeException("Aucun utilisateur connecté !"));

        Utilisateur userConnecte = utilisateurRepository
                .findDistinctByEmailWithPressing(emailConnecte.toLowerCase().trim())
                .orElseThrow(() -> new RuntimeException("Utilisateur connecté introuvable : " + emailConnecte));

        if (userConnecte.getPressing() == null)
            throw new RuntimeException("Aucun pressing associé à cet utilisateur !");

        commande.setPressing(userConnecte.getPressing());

        // Client obligatoire
        Client client = clientRepository.findById(commandeDTO.getClientId())
                .orElseThrow(() -> new RuntimeException("Client introuvable avec l'ID : " + commandeDTO.getClientId()));
        commande.setClient(client);

        // Paramètre pour calcul du montant
        if (commandeDTO.getParametreId() != null) {
            Parametre parametre = parametreRepository.findById(commandeDTO.getParametreId())
                    .orElseThrow(() -> new RuntimeException("Paramètre introuvable avec l'ID : " + commandeDTO.getParametreId()));
            commande.setParametre(parametre);
            double montantBrutCalcule = parametre.getPrix() * commande.getQte();
            commande.setMontantBrut(montantBrutCalcule);
        } else {
            commande.setMontantBrut(0.0);
        }

        // Remise et montant net
        double remise = commandeDTO.getRemise() != null ? commandeDTO.getRemise() : 0.0;
        commande.setRemise(remise);
        commande.setMontantNet(commande.getMontantBrut() - remise);

        // Dates
        LocalDate dateReception = Optional.ofNullable(commandeDTO.getDateReception()).orElse(LocalDate.now());
        commande.setDateReception(dateReception);
        LocalDate dateLivraison = commandeDTO.isExpress()
                ? dateReception.plusDays(1)
                : dateReception.plusDays(3);
        commande.setDateLivraison(dateLivraison);
        commande.setExpress(commandeDTO.isExpress());

        // Statut
        commande.setStatut(StatutCommande.EN_COURS);

        // Sauvegarde de la commande
        Commande saved = commandeRepository.save(commande);

        // Génération automatique du PDF
        commandePdfService.genererCommandePdf(saved);

        // Conversion en DTO
        return toDto(saved);
    }

    // Méthode pour convertir DTO -> Entity
    private Commande fromDto(CommandeDTO dto) {
        Commande commande = new Commande();
        commande.setQte(dto.getQte() != null ? dto.getQte() : 1);
        commande.setExpress(dto.isExpress());
        commande.setDateReception(dto.getDateReception());
        return commande;
    }

    // Méthode pour convertir Entity -> DTO
    public CommandeDTO toDto(Commande commande) {
        CommandeDTO dto = new CommandeDTO();
        dto.setId(commande.getId());

        if (commande.getClient() != null) {
            dto.setClientId(commande.getClient().getId());
            dto.setClientNom(commande.getClient().getNom());
            dto.setClientTelephone(commande.getClient().getTelephone());
        }

        if (commande.getParametre() != null) {
            Parametre p = commande.getParametre();
            dto.setParametreId(p.getId());
            dto.setArticle(p.getArticle());
            dto.setService(p.getService());
            dto.setPrix(p.getPrix());
        }

        dto.setQte(commande.getQte());
        dto.setMontantBrut(commande.getMontantBrut());
        dto.setRemise(commande.getRemise());
        dto.setMontantNet(commande.getMontantNet());
        dto.setExpress(commande.isExpress());
        dto.setDateReception(commande.getDateReception());
        dto.setDateLivraison(commande.getDateLivraison());
        dto.setStatut(commande.getStatut());

        return dto;
    }


    public List<CommandeDTO> getAllCommandes() {
        String emailConnecte = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(auth -> auth.getName())
                .orElseThrow(() -> new RuntimeException("Aucun utilisateur connecté !"));

        Utilisateur userConnecte = utilisateurRepository
                .findDistinctByEmailWithPressing(emailConnecte.toLowerCase().trim())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : " + emailConnecte));

        if (userConnecte.getPressing() == null) {
            throw new RuntimeException("Aucun pressing associé à l'utilisateur connecté !");
        }

        List<Commande> commandes = commandeRepository.findAllByPressing(userConnecte.getPressing());
        return commandes.stream().map(this::toDto).toList();
    }

    public CommandeDTO updateCommande(Long id, CommandeDTO dto) {
        String emailConnecte = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(auth -> auth.getName())
                .orElseThrow(() -> new RuntimeException("Aucun utilisateur connecté !"));

        Utilisateur userConnecte = utilisateurRepository
                .findDistinctByEmailWithPressing(emailConnecte.toLowerCase().trim())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : " + emailConnecte));

        if (userConnecte.getPressing() == null) {
            throw new RuntimeException("Aucun pressing associé à cet utilisateur !");
        }

        Commande commande = commandeRepository.findDistinctByIdWithPressing(id)
                .orElseThrow(() -> new RuntimeException("Commande introuvable : " + id));

        if (!commande.getPressing().getId().equals(userConnecte.getPressing().getId())) {
            throw new RuntimeException("Accès refusé : cette commande appartient à un autre pressing");
        }

        if (dto.getQte() != null) {
            commande.setQte(dto.getQte());
        }

        if (dto.getParametreId() != null) {
            Parametre param = parametreRepository.findById(dto.getParametreId())
                    .orElseThrow(() -> new RuntimeException("Paramètre introuvable : " + dto.getParametreId()));
            commande.setParametre(param);
            commande.setMontantBrut(param.getPrix() * commande.getQte());
        }

        double remise = dto.getRemise() != null ? dto.getRemise() : commande.getRemise();
        commande.setRemise(remise);
        commande.setMontantNet(commande.getMontantBrut() - remise);

        if (dto.getDateReception() != null) {
            commande.setDateReception(dto.getDateReception());
        }

        if (dto.isExpress() != commande.isExpress()) {
            commande.setExpress(dto.isExpress());
            commande.setDateLivraison(
                    dto.isExpress()
                            ? commande.getDateReception().plusDays(1)
                            : commande.getDateReception().plusDays(3)
            );
        }

        if (dto.getStatut() != null) {
            commande.setStatut(dto.getStatut());
        }

        Commande saved = commandeRepository.save(commande);
        return toDto(saved);
    }

    // 🔹 1. Total des commandes par jour
    public List<Map<String, Object>> getTotalCommandesParJour() {
        return mapToList(commandeRepository.countCommandesByDay());
    }

    // 🔹 2. Commandes EN_COURS par jour
    public List<Map<String, Object>> getCommandesEnCoursParJour() {
        return mapToList(commandeRepository.countCommandesByStatutAndDay(StatutCommande.EN_COURS));
    }

    // 🔹 3. Commandes LIVREE par jour
    public List<Map<String, Object>> getCommandesLivreesParJour() {
        return mapToList(commandeRepository.countCommandesByStatutAndDay(StatutCommande.LIVREE));
    }

    // 🔧 utilitaire pour formater les données
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
}
