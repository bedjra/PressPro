package com.press.pro.service;

import com.press.pro.Dto.CommandeDTO;
import com.press.pro.Entity.*;
import com.press.pro.enums.StatutCommande;
import com.press.pro.enums.StatutPaiement;
import com.press.pro.repository.*;
import com.press.pro.service.Pdf.CommandePdfService;
import com.press.pro.service.Pdf.StatutCommandePdfService;
import jakarta.transaction.Transactional;
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
    private CommandeLigneRepository commandeLigneRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ParametreRepository parametreRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private CommandePdfService commandePdfService;

    @Autowired
    private StatutCommandePdfService statutCommandePdfService;


    // Récupérer l'utilisateur connecté
    private Utilisateur getUserConnecte() {
        String email = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(auth -> auth.getName())
                .orElseThrow(() -> new RuntimeException("Aucun utilisateur connecté !"));

        return utilisateurRepository.findDistinctByEmailWithPressing(email.toLowerCase().trim())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : " + email));
    }

    // Sauvegarde d'une commande
    public Commande saveCommandeEntity(CommandeDTO dto) {

        Utilisateur user = getUserConnecte();

        if (dto.getClientId() == null)
            throw new RuntimeException("Le client est obligatoire");

        if (dto.getParametreIds() == null || dto.getParametreIds().isEmpty())
            throw new RuntimeException("Au moins un paramètre est obligatoire");

        if (dto.getQtes() == null || dto.getQtes().size() != dto.getParametreIds().size())
            throw new RuntimeException("Les quantités doivent correspondre aux paramètres");

        // Création de la commande
        Commande commande = new Commande();
        commande.setPressing(user.getPressing());
        commande.setStatut(StatutCommande.EN_COURS); // statut automatique
        commande.setDateReception(dto.getDateReception());
        commande.setDateLivraison(dto.getDateLivraison());
        commande.setRemise(dto.getRemiseGlobale() != null ? dto.getRemiseGlobale() : 0);

        // Client
        Client client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new RuntimeException("Client introuvable !"));
        commande.setClient(client);

        // Sauvegarde initiale
        commande = commandeRepository.save(commande);

        List<CommandeLigne> lignes = new ArrayList<>();

        // Création des lignes
        for (int i = 0; i < dto.getParametreIds().size(); i++) {
            Long paramId = dto.getParametreIds().get(i);
            int qte = dto.getQtes().get(i);

            Parametre param = parametreRepository.findById(paramId)
                    .orElseThrow(() -> new RuntimeException("Paramètre introuvable : " + paramId));

            CommandeLigne ligne = new CommandeLigne();
            ligne.setCommande(commande);
            ligne.setParametre(param);
            ligne.setQuantite(qte);

            ligne.recalcMontantBrut(); // calcule automatiquement le brut

            commandeLigneRepository.save(ligne);
            lignes.add(ligne);
        }

        commande.setLignes(lignes);

        // Calcul montant net total
        double totalBrut = lignes.stream().mapToDouble(CommandeLigne::getMontantBrut).sum();
        double remiseGlobale = commande.getRemise();

        for (CommandeLigne l : lignes) {
            double proportion = l.getMontantBrut() / totalBrut;
            double net = l.getMontantBrut() - (remiseGlobale * proportion);
            l.setMontantNet(net);
            commandeLigneRepository.save(l);
        }

        double totalNet = lignes.stream().mapToDouble(CommandeLigne::getMontantNet).sum();

        // Paiement global
        double montantPaye = dto.getMontantPaye() != null ? dto.getMontantPaye() : 0;
        commande.setMontantPaye(montantPaye);

        // 🔥 Détermination automatique du statut de paiement
        if (montantPaye == 0) {
            commande.setStatutPaiement(StatutPaiement.NON_PAYE);
        } else if (montantPaye < totalNet) {
            commande.setStatutPaiement(StatutPaiement.PARTIELLEMENT_PAYE);
        } else {
            commande.setStatutPaiement(StatutPaiement.PAYE);
        }

        // Sauvegarde finale
        return commandeRepository.save(commande);
    }



    // Sauvegarde + génération PDF


    public ResponseEntity<byte[]> saveCommandeEtTelechargerPdf(CommandeDTO dto) {

        Utilisateur user = getUserConnecte();  // ➤ obligatoire maintenant

        Commande saved = saveCommandeEntity(dto);

        byte[] pdf = commandePdfService.genererCommandePdf(saved, user);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=commande_" + saved.getId() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }


    // Conversion entity -> DTO
    public CommandeDTO toDto(Commande c) {
        CommandeDTO dto = new CommandeDTO();

        dto.setId(c.getId());
        dto.setClientId(c.getClient() != null ? c.getClient().getId() : null);
        dto.setClientNom(c.getClient() != null ? c.getClient().getNom() : null);

        dto.setRemiseGlobale(c.getRemise());
        dto.setMontantPaye(c.getMontantPaye());
        dto.setDateReception(c.getDateReception());
        dto.setDateLivraison(c.getDateLivraison());

        // 🔹 Statut de la commande
        dto.setStatut(c.getStatut());

        // 🔹 Statut du paiement (ajouté)
        dto.setStatutPaiement(c.getStatutPaiement() != null ? c.getStatutPaiement() : StatutPaiement.NON_PAYE);

        List<Long> paramIds = new ArrayList<>();
        List<Integer> qtes = new ArrayList<>();
        List<Double> brut = new ArrayList<>();
        List<Double> net = new ArrayList<>();

        if (c.getLignes() != null) {
            for (CommandeLigne l : c.getLignes()) {
                paramIds.add(l.getParametre().getId());
                qtes.add(l.getQuantite());
                brut.add(l.getMontantBrut());
                net.add(l.getMontantNet());
            }
        }

        dto.setParametreIds(paramIds);
        dto.setQtes(qtes);
        dto.setMontantsBruts(brut);
        dto.setMontantsNets(net);

        return dto;
    }

        public List<CommandeDTO> getAllCommandes() {
        Utilisateur user = getUserConnecte();
        return commandeRepository.findAllByPressing(user.getPressing())
                .stream().map(this::toDto).toList();
    }



    // ------------------------------------------------------
    // Changer le statut d'une commande et ajouter un paiement partiel
    // ------------------------------------------------------
    @Transactional
    public ResponseEntity<byte[]> updateStatutCommandeAvecPaiementPdf(Long commandeId,
                                                                      double montantActuel) {

        Utilisateur user = getUserConnecte();

        // Vérification propriétaire pressing
        Commande commande = commandeRepository
                .findDistinctByIdAndPressingId(commandeId, user.getPressing().getId())
                .orElseThrow(() -> new RuntimeException("Commande introuvable : " + commandeId));

        double montantAvant = commande.getMontantPaye();

        // 🔥 Mise à jour du statut : on force à LIVREE
        commande.setStatut(StatutCommande.LIVREE);

        // 🔥 Ajout du paiement si présent
        if (montantActuel > 0) {
            commande.setMontantPaye(montantAvant + montantActuel);
        }

        // 🔥 Sauvegarde
        Commande saved = commandeRepository.save(commande);

        // 🔥 Génération PDF : ancien montant, montant ajouté, nouvel état + utilisateur émetteur
        byte[] pdf = statutCommandePdfService.genererStatutPdf(
                saved,
                montantActuel,
                montantAvant,
                user
        );


        // 🔥 Retour du fichier en téléchargement
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=commande_statut_" + saved.getId() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }


    // ------------------------------------------------------
    // Récupérer une commande par id (avec vérif pressing)
    // ------------------------------------------------------
    public CommandeDTO getCommandeById(Long id) {
        Utilisateur user = getUserConnecte();

        Commande commande = commandeRepository
                .findDistinctByIdAndPressingId(id, user.getPressing().getId())
                .orElseThrow(() -> new RuntimeException("Commande introuvable ou accès refusé : " + id));

        // Conversion Entity -> DTO
        return toDto(commande);
    }





 //    ======================== 📊 STATISTIQUES JOURNALIERES ========================

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

   //  Chiffre d'affaires total
    public BigDecimal getChiffreAffairesTotal() {
        Utilisateur user = getUserConnecte();
        Long pressingId = user.getPressing().getId();

        return commandeRepository.sumMontantNetByPressing(pressingId);
    }

    // 🔹 Chiffre d'affaires journalier
    public Double getCAJournalier() {
        Utilisateur user = getUserConnecte();
        LocalDate today = LocalDate.now();

        return commandeRepository
                .sumMontantNetByDateAndPressing(today, user.getPressing().getId())
                .orElse(0.0);
    }

    // 🔹 Chiffre d'affaires hebdomadaire
    public Double getCAHebdomadaire() {
        Utilisateur user = getUserConnecte();
        LocalDate debut = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate fin = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        return Math.round(commandeRepository
                .sumMontantNetBetweenDatesAndPressing(debut, fin, user.getPressing().getId())
                .orElse(0.0) * 100.0) / 100.0;
    }

    // 🔹 Chiffre d'affaires mensuel
    public Double getCAMensuel() {
        Utilisateur user = getUserConnecte();
        LocalDate debut = LocalDate.now().withDayOfMonth(1);
        LocalDate fin = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        return Math.round(commandeRepository
                .sumMontantNetBetweenDatesAndPressing(debut, fin, user.getPressing().getId())
                .orElse(0.0) * 100.0) / 100.0;
    }

    // 🔹 Chiffre d'affaires annuel
    public Double getCAAnnuel() {
        Utilisateur user = getUserConnecte();
        LocalDate debut = LocalDate.now().withDayOfYear(1);
        LocalDate fin = LocalDate.now().with(TemporalAdjusters.lastDayOfYear());

        return Math.round(commandeRepository
                .sumMontantNetBetweenDatesAndPressing(debut, fin, user.getPressing().getId())
                .orElse(0.0) * 100.0) / 100.0;
    }




    // 🔹 Total impayés
    public Double getTotalImpayes() {
        Long pressingId = getUserConnecte().getPressing().getId();

        return commandeRepository
                .sumResteAPayerByPressingAndStatutPaiement(
                        pressingId,
                        List.of(StatutPaiement.NON_PAYE, StatutPaiement.PARTIELLEMENT_PAYE)
                )
                .orElse(0.0);
    }


}
