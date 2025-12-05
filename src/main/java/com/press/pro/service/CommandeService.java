package com.press.pro.service;

import com.press.pro.Dto.DtoCommande;
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

    @Autowired
    private TarifKiloRepository tarifKiloRepository;


    // Récupérer l'utilisateur connecté
    private Utilisateur getUserConnecte() {
        String email = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(auth -> auth.getName())
                .orElseThrow(() -> new RuntimeException("Aucun utilisateur connecté !"));

        return utilisateurRepository.findDistinctByEmailWithPressing(email.toLowerCase().trim())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : " + email));
    }


    // SAUVEGARDE D’UNE COMMANDE (ARTICLE OU KILO)
    // ------------------------------------------------------
    @Transactional
    public Commande saveCommandeEntity(DtoCommande dto) {

        Utilisateur user = getUserConnecte();

        // Vérification du client
        if (dto.getClientId() == null)
            throw new RuntimeException("Le client est obligatoire");

        // Vérification exclusivité article/kilo
        boolean hasArticles = dto.getParametreIds() != null && !dto.getParametreIds().isEmpty();
        boolean hasKilos = dto.getTarifKiloIds() != null && !dto.getTarifKiloIds().isEmpty();

        if (hasArticles && hasKilos) {
            throw new RuntimeException("Vous ne pouvez pas mélanger articles et kilos dans une même commande !");
        }
        if (!hasArticles && !hasKilos) {
            throw new RuntimeException("La commande doit contenir au moins un article ou un tarif kilo !");
        }

        // -------------------------------
        // Création de la commande
        // -------------------------------
        Commande commande = new Commande();
        commande.setPressing(user.getPressing());
        commande.setStatut(StatutCommande.EN_COURS);
        commande.setDateReception(dto.getDateReception());
        commande.setDateLivraison(dto.getDateLivraison());
        commande.setRemise(dto.getRemiseGlobale() != null ? dto.getRemiseGlobale() : 0);

        // Client
        Client client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new RuntimeException("Client introuvable !"));
        commande.setClient(client);

        // Sauvegarde initiale pour récupérer l'id
        commande = commandeRepository.save(commande);

        List<CommandeLigne> lignes = new ArrayList<>();

        // --------------------------------------------------
        // 🔥 CAS 1 : LIGNES PAR ARTICLE
        // --------------------------------------------------
        if (hasArticles) {

            if (dto.getQuantites() == null || dto.getQuantites().size() != dto.getParametreIds().size())
                throw new RuntimeException("Les quantités doivent correspondre aux paramètres");

            for (int i = 0; i < dto.getParametreIds().size(); i++) {
                Long paramId = dto.getParametreIds().get(i);
                int qte = dto.getQuantites().get(i);

                Parametre param = parametreRepository.findById(paramId)
                        .orElseThrow(() -> new RuntimeException("Paramètre introuvable : " + paramId));

                CommandeLigne ligne = new CommandeLigne();
                ligne.setCommande(commande);
                ligne.setParametre(param);
                ligne.setQuantite(qte);

                // calcul automatique du brut
                ligne.recalcMontantBrut();

                commandeLigneRepository.save(ligne);
                lignes.add(ligne);
            }
        }

        // --------------------------------------------------
        // 🔥 CAS 2 : LIGNES PAR KILO
        // --------------------------------------------------
        if (hasKilos) {

            if (dto.getPoids() == null || dto.getPoids().size() != dto.getTarifKiloIds().size())
                throw new RuntimeException("Les poids doivent correspondre aux tarifs kilo");

            for (int i = 0; i < dto.getTarifKiloIds().size(); i++) {
                Long tarifId = dto.getTarifKiloIds().get(i);
                Double kg = dto.getPoids().get(i);

                TarifKilo tarif = tarifKiloRepository.findById(tarifId)
                        .orElseThrow(() -> new RuntimeException("Tarif kilo introuvable : " + tarifId));

                CommandeLigne ligne = new CommandeLigne();
                ligne.setCommande(commande);
                ligne.setTarifKilo(tarif);
                ligne.setPoids(kg);

                // montant brut = prix unitaire * poids
                ligne.setMontantBrut(tarif.getPrix() * kg);

                commandeLigneRepository.save(ligne);
                lignes.add(ligne);
            }
        }

        commande.setLignes(lignes);

        // --------------------------------------------------
        // 🔥 CALCUL DU NET (avec remise répartie)
        // --------------------------------------------------
        double totalBrut = lignes.stream().mapToDouble(CommandeLigne::getMontantBrut).sum();
        double remiseGlobale = commande.getRemise();

        for (CommandeLigne l : lignes) {
            double proportion = l.getMontantBrut() / totalBrut;
            double net = l.getMontantBrut() - (remiseGlobale * proportion);
            l.setMontantNet(net);
            commandeLigneRepository.save(l);
        }

        double totalNet = lignes.stream().mapToDouble(CommandeLigne::getMontantNet).sum();

        // --------------------------------------------------
        // 🔥 PAIEMENT
        // --------------------------------------------------
        double montantPaye = dto.getMontantPaye() != null ? dto.getMontantPaye() : 0;
        commande.setMontantPaye(montantPaye);

        if (montantPaye == 0) commande.setStatutPaiement(StatutPaiement.NON_PAYE);
        else if (montantPaye < totalNet) commande.setStatutPaiement(StatutPaiement.PARTIELLEMENT_PAYE);
        else commande.setStatutPaiement(StatutPaiement.PAYE);

        return commandeRepository.save(commande);
    }

    // Sauvegarde + génération PDF
    public ResponseEntity<byte[]> saveCommandeEtTelechargerPdf(DtoCommande dto) {
        Commande saved = saveCommandeEntity(dto);
        Utilisateur user = getUserConnecte();

        byte[] pdf = commandePdfService.genererCommandePdf(saved, user);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=commande_" + saved.getId() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }



    // Conversion entity -> DTO
    public DtoCommande toDto(Commande c) {
        DtoCommande dto = new DtoCommande();

        dto.setId(c.getId());
        dto.setClientId(c.getClient() != null ? c.getClient().getId() : null);
        dto.setClientNom(c.getClient() != null ? c.getClient().getNom() : null);

        dto.setRemiseGlobale(c.getRemise());
        dto.setMontantPaye(c.getMontantPaye());
        dto.setDateReception(c.getDateReception());
        dto.setDateLivraison(c.getDateLivraison());

        dto.setStatut(c.getStatut());
        dto.setStatutPaiement(
                c.getStatutPaiement() != null ? c.getStatutPaiement() : StatutPaiement.NON_PAYE
        );

        // -------- Collections DTO --------
        List<Long> paramIds = new ArrayList<>();
        List<Integer> qtes = new ArrayList<>();
        List<Double> poidsList = new ArrayList<>();
        List<Long> kiloIds = new ArrayList<>();

        List<Double> brut = new ArrayList<>();
        List<Double> net = new ArrayList<>();

        // ---------------------------
        //     LIGNES DE COMMANDE
        // ---------------------------
        if (c.getLignes() != null) {
            for (CommandeLigne l : c.getLignes()) {

                // 🔥 CAS ARTICLE
                if (l.getParametre() != null) {
                    paramIds.add(l.getParametre().getId());
                    qtes.add(l.getQuantite());
                    kiloIds.add(null);
                    poidsList.add(null);
                }

                // 🔥 CAS KILO
                else if (l.getTarifKilo() != null) {
                    kiloIds.add(l.getTarifKilo().getId());
                    poidsList.add(l.getPoids());
                    paramIds.add(null);
                    qtes.add(null);
                }

                brut.add(l.getMontantBrut());
                net.add(l.getMontantNet());
            }
        }

        dto.setParametreIds(paramIds);
        dto.setQuantites(qtes);

        dto.setTarifKiloIds(kiloIds);
        dto.setPoids(poidsList);

        dto.setMontantsBruts(brut);
        dto.setMontantsNets(net);

        return dto;
    }

    public List<DtoCommande> getAllCommandes() {
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
    // DELETE une commande par id (avec vérif pressing)
    // ------------------------------------------------------
    @Transactional
    public void deleteCommandeById(Long commandeId) {
        Utilisateur user = getUserConnecte();

        Commande commande = commandeRepository
                .findDistinctByIdAndPressingId(commandeId, user.getPressing().getId())
                .orElseThrow(() -> new RuntimeException("Commande introuvable ou non autorisée : " + commandeId));

        commandeRepository.delete(commande);
    }


    // ------------------------------------------------------
    // Récupérer une commande par id (avec vérif pressing)
    // ------------------------------------------------------
    public DtoCommande getCommandeById(Long id) {
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

        List<Object[]> results = commandeRepository.countCommandesEnCoursAujourdHui(
                pressingId,
                StatutCommande.EN_COURS
        );

        long nbCommandes = getNbCommandes(results);

        return Map.of(
                "date", LocalDate.now(),
                "nbCommandes", nbCommandes
        );
    }




    public Map<String, Object> getCommandesLivreesParJour() {
        Long pressingId = getUserConnecte().getPressing().getId();
        LocalDate today = LocalDate.now();

        List<Object[]> results = commandeRepository.countLivrees(
                StatutCommande.LIVREE,
                pressingId,
                today
        );

        long nb = getNbCommandes(results);

        return Map.of(
                "dateLivraison", today,
                "nbCommandes", nb
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
