package com.press.pro.service;

import com.press.pro.Dto.CAMoisDTO;
import com.press.pro.Dto.DtoCommande;
import com.press.pro.Dto.DtoCommandeSimple;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;


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
    private PaiementRepository paiementRepository;


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

        double montantVerse = dto.getMontantPaye() != null ? dto.getMontantPaye() : 0;

// 🚨 Interdire montant négatif
        if (montantVerse < 0) {
            throw new IllegalArgumentException(
                    "Le montant payé ne peut pas être négatif."
            );
        }

// 🚨 Interdire dépassement du reste à payer
        if (montantVerse > commande.getResteAPayer()) {
            throw new IllegalArgumentException(
                    "Le montant payé (" + montantVerse +
                            ") dépasse le reste à payer (" + commande.getResteAPayer() + ")."
            );
        }

// 🔥 1️⃣ Créer un enregistrement Paiement
        if (montantVerse > 0) {
            Paiement paiement = new Paiement();
            paiement.setCommande(commande);
            paiement.setPressing(commande.getPressing());
            paiement.setMontant(montantVerse);

            paiementRepository.save(paiement);
        }

// 🔥 2️⃣ Ajouter au cumul
        double nouveauTotalPaye = commande.getMontantPaye() + montantVerse;
        commande.setMontantPaye(nouveauTotalPaye);

// 🔥 3️⃣ Recalcul automatique
        commande.recalculerPaiement();

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
        dto.setResteAPayer(c.getResteAPayer());
        dto.setReliquat(c.getReliquat());  // <-- Ajout du reliquat dans le DTO
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

        if (c.getLignes() != null) {
            for (CommandeLigne l : c.getLignes()) {
                if (l.getParametre() != null) {
                    paramIds.add(l.getParametre().getId());
                    qtes.add(l.getQuantite());
                    kiloIds.add(null);
                    poidsList.add(null);
                } else if (l.getTarifKilo() != null) {
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
    public ResponseEntity<byte[]> updateStatutCommandeAvecPaiementPdf(
            Long commandeId,
            double montantActuel,
            Double reliquat
    ) {

        Utilisateur user = getUserConnecte();

        Commande commande = commandeRepository
                .findDistinctByIdAndPressingId(commandeId, user.getPressing().getId())
                .orElseThrow(() -> new RuntimeException("Commande introuvable : " + commandeId));

        double montantAvant = commande.getMontantPaye();
        double totalNet = commande.getMontantNetTotal();

        // 🚨 Sécurité : montant négatif
        if (montantActuel < 0) {
            throw new IllegalArgumentException("Le montant payé ne peut pas être négatif.");
        }

        // 🚨 Vérifier dépassement
        double nouveauTotal = montantAvant + montantActuel;

        if (nouveauTotal > totalNet) {
            throw new IllegalArgumentException(
                    "Le montant total payé dépasse le total de la commande."
            );
        }

        // 🔹 Mettre statut livré
        commande.setStatut(StatutCommande.LIVREE);
        commande.setDateLivraison(LocalDate.now());

        // 🔥 1️⃣ Enregistrer un paiement si montant > 0
        if (montantActuel > 0) {
            Paiement paiement = new Paiement();
            paiement.setCommande(commande);
            paiement.setPressing(commande.getPressing());
            paiement.setMontant(montantActuel);

            paiementRepository.save(paiement);

            // Ajouter au cumul
            commande.setMontantPaye(nouveauTotal);
        }

        // 🔹 Reliquat optionnel
        double reliquatEffectif = reliquat != null ? reliquat : 0;
        commande.setReliquat(reliquatEffectif);

        // 🔹 Recalcul métier
        commande.recalculerPaiement();

        Commande saved = commandeRepository.save(commande);

        // 🔹 Génération PDF
        byte[] pdf = statutCommandePdfService.genererStatutPdf(
                saved,
                montantActuel,
                montantAvant,
                user
        );

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




    // 🔹 Chiffre d'affaires total encaissé
//    public BigDecimal getChiffreAffairesTotal() {
//        Long pressingId = getUserConnecte().getPressing().getId();
//        Long montant = commandeRepository.getChiffreAffaires(pressingId);
//        return BigDecimal.valueOf(montant != null ? montant : 0L);
//    }

    // 🔹 Chiffre d'affaires total encaissé
    public BigDecimal getChiffreAffairesTotal() {
        Long pressingId = getUserConnecte().getPressing().getId(); // assure-toi que c’est le bon pressing

        // Vérification/debug
        List<Commande> commandes = commandeRepository.findAllByPressingIdAndStatutPaiement(pressingId, StatutPaiement.PAYE);
        commandes.forEach(c -> System.out.println(c.getId() + " : " + c.getMontantPaye()));

        // Somme
        Long montant = commandeRepository.getChiffreAffaires(pressingId, StatutPaiement.PAYE);

        return BigDecimal.valueOf(montant != null ? montant : 0L);
    }




    // 🔹 Chiffre d'affaires journalier



    public BigDecimal getCAJournalier() {

        Utilisateur user = getUserConnecte();
        Long pressingId = user.getPressing().getId();

        return paiementRepository.sumCAJournalier(pressingId);
    }



    // 🔹 Chiffre d'affaires hebdomadaire


    public Double getCAHebdomadaire() {

        Utilisateur user = getUserConnecte();
        Long pressingId = user.getPressing().getId();

        LocalDateTime debut = LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .atStartOfDay();

        LocalDateTime fin = LocalDate.now()
                .with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                .atTime(23, 59, 59);

        Double ca = paiementRepository
                .sumPaiementsEntreDates(debut, fin, pressingId)
                .orElse(0.0);

        return Math.round(ca * 100.0) / 100.0;
    }


    // 🔹 Chiffre d'affaires mensuel
    public Double getCAMensuel() {

        Utilisateur user = getUserConnecte();
        Long pressingId = user.getPressing().getId();

        LocalDateTime debut = LocalDate.now()
                .withDayOfMonth(1)
                .atStartOfDay();

        LocalDateTime fin = LocalDate.now()
                .withDayOfMonth(LocalDate.now().lengthOfMonth())
                .atTime(23, 59, 59);

        Double ca = paiementRepository
                .sumPaiementsEntreDates(debut, fin, pressingId)
                .orElse(0.0);

        return Math.round(ca * 100.0) / 100.0;
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
        return commandeRepository.sumResteAPayerByPressing(pressingId);
    }


    public List<DtoCommandeSimple> getDetailsCommandesLivreesParJour() {
        Long pressingId = getUserConnecte().getPressing().getId();
        LocalDate today = LocalDate.now();

        List<Commande> commandes = commandeRepository.findByStatutAndPressingIdAndDateLivraison(
                StatutCommande.LIVREE,
                pressingId,
                today
        );

        return commandes.stream()
                .map(c -> {
                    DtoCommandeSimple dto = new DtoCommandeSimple();
                    dto.setClientNom(c.getClient().getNom());
                    dto.setDateReception(c.getDateReception());
                    dto.setDateLivraison(c.getDateLivraison());
                    dto.setMontantPaye(c.getMontantPaye());
                    dto.setResteAPayer(c.getResteAPayer());
                    dto.setReliquat(c.getReliquat());
                    return dto;
                })
                .collect(Collectors.toList());
    }







    public Double getCAMensuel(int mois, int annee) {

        Utilisateur user = getUserConnecte();
        Long pressingId = user.getPressing().getId();

        LocalDateTime debut = LocalDate.of(annee, mois, 1)
                .atStartOfDay();

        LocalDateTime fin = LocalDate.of(annee, mois, 1)
                .withDayOfMonth(LocalDate.of(annee, mois, 1).lengthOfMonth())
                .atTime(23, 59, 59);

        Double ca = paiementRepository
                .sumPaiementsEntreDates(debut, fin, pressingId)
                .orElse(0.0);

        return Math.round(ca * 100.0) / 100.0;
    }

    public Map<Integer, Map<Integer, Double>> getCAParMoisStructure() {

        Utilisateur user = getUserConnecte();
        List<Object[]> resultats =
                commandeRepository.sumCAParMois(user.getPressing().getId());

        Map<Integer, Map<Integer, Double>> data = new LinkedHashMap<>();

        for (Object[] row : resultats) {

            Integer annee = ((Number) row[0]).intValue();
            Integer mois = ((Number) row[1]).intValue();
            Double total = ((Number) row[2]).doubleValue();

            data
                    .computeIfAbsent(annee, k -> new LinkedHashMap<>())
                    .put(mois, Math.round(total * 100.0) / 100.0);
        }

        return data;
    }

}
