package com.press.pro.service;

import com.press.pro.Dto.CommandeDTO;
import com.press.pro.Entity.Client;
import com.press.pro.Entity.Commande;
import com.press.pro.Entity.Parametre;
import com.press.pro.Entity.Utilisateur;
import com.press.pro.enums.StatutCommande;
import com.press.pro.enums.StatutPaiement;
import com.press.pro.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
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
    private ChargeRepository chargeRepository;


    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private CommandePdfService commandePdfService;

    // 🔹 Récupération de l'utilisateur connecté
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

    // 🔹 Création / Enregistrement d'une commande
    public CommandeDTO saveCommande(CommandeDTO dto) {
        if (dto.getClientId() == null) {
            throw new RuntimeException("Le client est obligatoire");
        }

        // Récupération de l'utilisateur connecté et du pressing associé
        Utilisateur user = getUserConnecte();

        // Conversion DTO -> Entity
        Commande commande = fromDto(dto);
        commande.setPressing(user.getPressing());

        // Lier le client
        Client client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new RuntimeException("Client introuvable : " + dto.getClientId()));
        commande.setClient(client);

        // Appliquer les paramètres et calculer les montants
        if (dto.getParametreId() != null) {
            applyParametreEtMontant(commande, dto.getParametreId());
        }

        // Appliquer remise et montant net
        applyRemiseEtNet(commande, dto.getRemise());

        // Appliquer les dates (reception + livraison express ou standard)
        applyDates(commande, dto.isExpress(), dto.getDateReception());

        // Statut de la commande
        commande.setStatut(StatutCommande.EN_COURS);

        // 🧠 Gestion du paiement
        double montantPaye = dto.getMontantPaye() != null ? dto.getMontantPaye() : 0;
        commande.setMontantPaye(montantPaye); // met automatiquement à jour le statutPaiement

        // Optionnel : forcer le statutPaiement si fourni
        if (dto.getStatutPaiement() != null) {
            commande.setStatutPaiement(dto.getStatutPaiement());
        }

        // Sauvegarde dans la base
        Commande saved = commandeRepository.save(commande);

        // Génération PDF automatique
        commandePdfService.genererCommandePdf(saved);

        // Conversion Entity -> DTO pour renvoi
        CommandeDTO result = toDto(saved);
        result.setMontantPaye(saved.getMontantPaye());
        result.setResteAPayer(saved.getResteAPayer());

        return result;
    }

    // 🔹 Création + génération PDF directe
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

        if (dto.getDateReception() != null)
            commande.setDateReception(dto.getDateReception());

        if (dto.isExpress() != commande.isExpress())
            applyDates(commande, dto.isExpress(), commande.getDateReception());

        if (dto.getStatut() != null)
            commande.setStatut(dto.getStatut());

        // 🧠 Mise à jour du statut de paiement si fourni
        if (dto.getStatutPaiement() != null)
            commande.setStatutPaiement(dto.getStatutPaiement());

        return toDto(commandeRepository.save(commande));
    }


    // 🔹 Méthodes utilitaires
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



    // 🔹 Récupération de toutes les commandes du pressing connecté
    public List<CommandeDTO> getAllCommandes() {
        Utilisateur user = getUserConnecte();
        return commandeRepository.findAllByPressing(user.getPressing())
                .stream().map(this::toDto).toList();
    }

    // 🔹 Génération PDF pour une commande existante
    public ResponseEntity<byte[]> telechargerCommandePdf(Long id) {
        Commande commande = commandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande introuvable : " + id));

        byte[] pdf = commandePdfService.genererCommandePdf(commande);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=commande_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }








    // 🔹 Conversion DTO -> Entity
    private Commande fromDto(CommandeDTO dto) {
        Commande c = new Commande();
        c.setQte(dto.getQte() != null ? dto.getQte() : 1);
        c.setExpress(dto.isExpress());
        c.setDateReception(dto.getDateReception());
        c.setStatutPaiement(dto.getStatutPaiement() != null ? dto.getStatutPaiement() : StatutPaiement.NON_PAYE);
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
        dto.setStatutPaiement(c.getStatutPaiement()); // ✅ ajouté
        return dto;
    }



    // 🔹 Chiffre d’affaires du jour
    public Double getCAJournalier() {
        Utilisateur user = getUserConnecte();
        LocalDate today = LocalDate.now();

        return commandeRepository
                .sumMontantNetByDateAndPressing(today, user.getPressing().getId())
                .orElse(0.0);
    }

    // 🔹 Chiffre d’affaires hebdomadaire
    public Double getCAHebdomadaire() {
        Utilisateur user = getUserConnecte();
        LocalDate debut = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate fin = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        Double caBrut = commandeRepository
                .sumMontantNetBetweenDatesAndPressing(debut, fin, user.getPressing().getId())
                .orElse(0.0);

        return Math.round(caBrut * 100.0) / 100.0;
    }

    // 🔹 Chiffre d’affaires mensuel
    public Double getCAMensuel() {
        Utilisateur user = getUserConnecte();
        LocalDate debut = LocalDate.now().withDayOfMonth(1);
        LocalDate fin = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        Double caBrut = commandeRepository
                .sumMontantNetBetweenDatesAndPressing(debut, fin, user.getPressing().getId())
                .orElse(0.0);

        return Math.round(caBrut * 100.0) / 100.0;
    }

    // 🔹 Chiffre d’affaires annuel
    public Double getCAAnnuel() {
        Utilisateur user = getUserConnecte();
        LocalDate debut = LocalDate.now().withDayOfYear(1);
        LocalDate fin = LocalDate.now().with(TemporalAdjusters.lastDayOfYear());

        Double caBrut = commandeRepository
                .sumMontantNetBetweenDatesAndPressing(debut, fin, user.getPressing().getId())
                .orElse(0.0);

        return Math.round(caBrut * 100.0) / 100.0;
    }





    public Double getTotalImpayes() {
        Utilisateur user = getUserConnecte();
        return commandeRepository.sumResteAPayerByPressingAndStatutPaiement(
                user.getPressing().getId(),
                List.of(StatutPaiement.NON_PAYE, StatutPaiement.PARTIELLEMENT_PAYE)
        ).orElse(0.0);
    }




    // 🔹 Changer le statut d'une commande
//    public CommandeDTO updateStatutCommande(Long commandeId, StatutCommande nouveauStatut) {
//        // 🔹 Récupération de l'utilisateur connecté
//        Utilisateur user = getUserConnecte();
//
//        // 🔹 Recherche de la commande dans le pressing du user
//        Commande commande = commandeRepository
//                .findDistinctByIdAndPressingId(commandeId, user.getPressing().getId())
//                .orElseThrow(() -> new RuntimeException("Commande introuvable ou accès refusé : " + commandeId));
//
//        // 🔹 Mise à jour du statut
//        commande.setStatut(nouveauStatut);
//
//        // 🧠 Si la commande est livrée, elle est considérée comme payée
//        if (nouveauStatut == StatutCommande.LIVREE) {
//            commande.setMontantPaye(commande.getMontantNet());
//            commande.setStatutPaiement(StatutPaiement.PAYE);
//        }
//
//        // 🔹 Sauvegarde
//        Commande saved = commandeRepository.save(commande);
//
//        // 🔹 Conversion en DTO
//        CommandeDTO dto = new CommandeDTO();
//        dto.setId(saved.getId());
//        dto.setExpress(saved.isExpress());
//        dto.setDateReception(saved.getDateReception());
//        dto.setDateLivraison(saved.getDateLivraison());
//        dto.setStatut(saved.getStatut());
//        dto.setStatutPaiement(saved.getStatutPaiement());
//
//        // --- Client ---
//        if (saved.getClient() != null) {
//            dto.setClientId(saved.getClient().getId());
//            dto.setClientNom(saved.getClient().getNom());
//            dto.setClientTelephone(saved.getClient().getTelephone());
//        }
//
//        // --- Paramètre (article, service, prix) ---
//        if (saved.getParametre() != null) {
//            dto.setParametreId(saved.getParametre().getId());
//            dto.setArticle(saved.getParametre().getArticle());
//            dto.setService(saved.getParametre().getService());
//            dto.setPrix(saved.getParametre().getPrix());
//        }
//
//        // --- Montants ---
//        dto.setQte(saved.getQte());
//        dto.setMontantBrut(saved.getMontantBrut());
//        dto.setRemise(saved.getRemise());
//        dto.setMontantNet(saved.getMontantNet());
//        dto.setMontantPaye(saved.getMontantPaye());
//        dto.setResteAPayer(0.0);
//
//
//        return dto;
//    }
//



    // 🔹 Changer le statut et éventuellement ajouter un paiement partiel
    public CommandeDTO updateStatutCommandeAvecPaiement(Long commandeId,
                                                        StatutCommande nouveauStatut,
                                                        double montantActuel) {
        Utilisateur user = getUserConnecte();

        Commande commande = commandeRepository
                .findDistinctByIdAndPressingId(commandeId, user.getPressing().getId())
                .orElseThrow(() -> new RuntimeException("Commande introuvable ou accès refusé : " + commandeId));

        // 🔹 Mettre à jour le statut
        commande.setStatut(nouveauStatut);

        // 🔹 Ajouter le paiement partiel
        if (montantActuel > 0) {
            double nouveauMontantPaye = commande.getMontantPaye() + montantActuel;
            commande.setMontantPaye(nouveauMontantPaye);
            // statut de paiement mis à jour automatiquement via setMontantPaye
        }

        // 🔹 Sauvegarde
        Commande saved = commandeRepository.save(commande);

        // 🔹 Conversion DTO
        CommandeDTO dto = new CommandeDTO();
        dto.setId(saved.getId());
        dto.setMontantNet(saved.getMontantNet());
        dto.setMontantPaye(saved.getMontantPaye());
        dto.setResteAPayer(saved.getResteAPayer());
        dto.setStatut(saved.getStatut());
        dto.setStatutPaiement(saved.getStatutPaiement());

        return dto;
    }



    public CommandeDTO getCommandeById(Long id) {
        // 🔹 Récupération de l'utilisateur connecté
        Utilisateur user = getUserConnecte();

        // 🔹 Recherche de la commande dans le pressing du user
        Commande commande = commandeRepository
                .findDistinctByIdAndPressingId(id, user.getPressing().getId())
                .orElseThrow(() -> new RuntimeException("Commande introuvable ou accès refusé : " + id));

        // 🔹 Conversion Entity -> DTO
        CommandeDTO dto = new CommandeDTO();

        // --- Informations principales ---
        dto.setId(commande.getId());
        dto.setExpress(commande.isExpress());
        dto.setDateReception(commande.getDateReception());
        dto.setDateLivraison(commande.getDateLivraison());
        dto.setStatut(commande.getStatut());
        dto.setStatutPaiement(commande.getStatutPaiement());

        // --- Client ---
        if (commande.getClient() != null) {
            dto.setClientId(commande.getClient().getId());
            dto.setClientNom(commande.getClient().getNom());
            dto.setClientTelephone(commande.getClient().getTelephone());
        }

        // --- Paramètre (article, service, prix) ---
        if (commande.getParametre() != null) {
            dto.setParametreId(commande.getParametre().getId());
            dto.setArticle(commande.getParametre().getArticle());
            dto.setService(commande.getParametre().getService());
            dto.setPrix(commande.getParametre().getPrix());
        }

        // --- Montants ---
        dto.setQte(commande.getQte());
        dto.setMontantBrut(commande.getMontantBrut());
        dto.setRemise(commande.getRemise());
        dto.setMontantNet(commande.getMontantNet());
        dto.setMontantPaye(commande.getMontantPaye());
        dto.setResteAPayer(commande.getResteAPayer());

        return dto;
    }



    // ======================== 📊 STATISTIQUES JOURNALIERES ========================

    private long getNbCommandes(List<Object[]> results) {
        return results.stream()
                .map(r -> ((Number) r[1]).longValue())
                .findFirst()
                .orElse(0L);
    }

    public Map<String, Object> getTotalCommandesParJour() {
        Long pressingId = getUserConnecte().getPressing().getId();
        LocalDate today = LocalDate.now();

        List<Object[]> results = commandeRepository.countCommandesByPressingAndDate(pressingId, today);
        long nbCommandes = getNbCommandes(results);

        return Map.of(
                "dateReception", today,
                "nbCommandes", nbCommandes
        );
    }

    public Map<String, Object> getCommandesEnCoursParJour() {
        Long pressingId = getUserConnecte().getPressing().getId();
        LocalDate today = LocalDate.now();

        List<Object[]> results = commandeRepository.countCommandesByStatutAndPressingAndDate(
                StatutCommande.EN_COURS, pressingId, today
        );
        long nbCommandes = getNbCommandes(results);

        return Map.of(
                "dateReception", today,
                "nbCommandes", nbCommandes
        );
    }

    public Map<String, Object> getCommandesLivreesParJour() {
        Long pressingId = getUserConnecte().getPressing().getId();
        LocalDate today = LocalDate.now();

        List<Object[]> results = commandeRepository.countCommandesByStatutAndPressingAndDate(
                StatutCommande.LIVREE, pressingId, today
        );
        long nbCommandes = getNbCommandes(results);

        return Map.of(
                "dateReception", today,
                "nbCommandes", nbCommandes
        );
    }

    // Chiffre d'affaires total
    public BigDecimal getChiffreAffairesTotal() {
        Utilisateur user = getUserConnecte();
        Long pressingId = user.getPressing().getId();

        return commandeRepository.sumMontantNetByPressing(pressingId);
    }

}
