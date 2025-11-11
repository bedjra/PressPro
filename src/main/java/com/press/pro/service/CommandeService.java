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



    // 🔹 Statistiques journalières filtrées par pressing
    public List<Map<String, Object>> getTotalCommandesParJour() {
        Long pressingId = getUserConnecte().getPressing().getId();
        return mapToList(commandeRepository.countCommandesByDayAndPressing(pressingId));
    }

    public List<Map<String, Object>> getCommandesEnCoursParJour() {
        Long pressingId = getUserConnecte().getPressing().getId();
        return mapToList(commandeRepository.countCommandesByStatutAndDayAndPressing(StatutCommande.EN_COURS, pressingId));
    }

    public List<Map<String, Object>> getCommandesLivreesParJour() {
        Long pressingId = getUserConnecte().getPressing().getId();
        return mapToList(commandeRepository.countCommandesByStatutAndDayAndPressing(StatutCommande.LIVREE, pressingId));
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



    // 🔹 Chiffre d’affaires du jour
    public Double getCAJournalier() {
        Utilisateur user = getUserConnecte();
        LocalDate today = LocalDate.now();

        Double caBrut = commandeRepository
                .sumMontantNetByDateAndPressing(today, user.getPressing().getId())
                .orElse(0.0);

        Double charges = chargeRepository
                .sumChargesByDateAndPressing(today, user.getPressing().getId());

        return caBrut - charges;
    }

    // 🔹 Chiffre d’affaires hebdomadaire
    public Double getCAHebdomadaire() {
        Utilisateur user = getUserConnecte();
        LocalDate debut = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate fin = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        Double caBrut = commandeRepository
                .sumMontantNetBetweenDatesAndPressing(debut, fin, user.getPressing().getId())
                .orElse(0.0);

        Double charges = chargeRepository
                .sumChargesBetweenDatesAndPressing(debut, fin, user.getPressing().getId());

        return Math.round((caBrut - charges) * 100.0) / 100.0;
    }

    // 🔹 Chiffre d’affaires mensuel
    public Double getCAMensuel() {
        Utilisateur user = getUserConnecte();
        LocalDate debut = LocalDate.now().withDayOfMonth(1);
        LocalDate fin = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        Double caBrut = commandeRepository
                .sumMontantNetBetweenDatesAndPressing(debut, fin, user.getPressing().getId())
                .orElse(0.0);

        Double charges = chargeRepository
                .sumChargesBetweenDatesAndPressing(debut, fin, user.getPressing().getId());

        return Math.round((caBrut - charges) * 100.0) / 100.0;
    }

    // 🔹 Chiffre d’affaires annuel
    public Double getCAAnnuel() {
        Utilisateur user = getUserConnecte();
        LocalDate debut = LocalDate.now().withDayOfYear(1);
        LocalDate fin = LocalDate.now().with(TemporalAdjusters.lastDayOfYear());

        Double caBrut = commandeRepository
                .sumMontantNetBetweenDatesAndPressing(debut, fin, user.getPressing().getId())
                .orElse(0.0);

        Double charges = chargeRepository
                .sumChargesBetweenDatesAndPressing(debut, fin, user.getPressing().getId());

        return Math.round((caBrut - charges) * 100.0) / 100.0;
    }




    public Double getTotalImpayes() {
        Utilisateur user = getUserConnecte();
        return commandeRepository.sumResteAPayerByPressingAndStatutPaiement(
                user.getPressing().getId(),
                List.of(StatutPaiement.NON_PAYE, StatutPaiement.PARTIELLEMENT_PAYE)
        ).orElse(0.0);
    }


    // 🔹 Changer le statut d'une commande
    public CommandeDTO updateStatutCommande(Long commandeId, StatutCommande nouveauStatut) {
        // Récupérer la commande
        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new RuntimeException("Commande introuvable : " + commandeId));

        // Mettre à jour le statut
        commande.setStatut(nouveauStatut);

        // Sauvegarder
        Commande saved = commandeRepository.save(commande);

        // Retourner DTO
        return toDto(saved);
    }



}
