package com.press.pro.service;

import com.press.pro.Dto.CommandeDTO;
import com.press.pro.Entity.Client;
import com.press.pro.Entity.Commande;
import com.press.pro.Entity.Parametre;
import com.press.pro.Entity.Utilisateur;
import com.press.pro.enums.StatutCommande;
import com.press.pro.enums.StatutPaiement;
import com.press.pro.repository.*;
import com.press.pro.service.Pdf.CommandePdfService;
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
    private KiloRepository kiloRepository; // ✅ Ajout

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
    public Commande saveCommandeEntity(CommandeDTO dto) {
        if (dto.getClientId() == null) {
            throw new RuntimeException("Le client est obligatoire");
        }

        // ✅ DEBUG: Afficher le type reçu
        System.out.println("DEBUG - typeKilogramme reçu: " + dto.getTypeKilogramme());
        System.out.println("DEBUG - kilo: " + dto.getKilo());
        System.out.println("DEBUG - prixParKg: " + dto.getPrixParKg());
        System.out.println("DEBUG - parametreId: " + dto.getParametreId());

        Utilisateur user = getUserConnecte();
        Commande commande = fromDto(dto);
        commande.setPressing(user.getPressing());

        // Lier le client
        Client client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new RuntimeException("Client introuvable : " + dto.getClientId()));
        commande.setClient(client);

        // Assignation des dates
        commande.setDateReception(dto.getDateReception() != null ? dto.getDateReception() : LocalDate.now());
        commande.setDateLivraison(dto.getDateLivraison() != null ? dto.getDateLivraison() : LocalDate.now().plusDays(3));

        // ✅ **Logique de facturation selon le type**
        if (Boolean.TRUE.equals(dto.getTypeKilogramme())) {
            System.out.println("DEBUG - Mode KILOGRAMME détecté");
            // 🔹 MODE KILOGRAMME
            applyFacturationKilogramme(commande, dto);
        } else {
            System.out.println("DEBUG - Mode ARTICLE détecté");
            // 🔹 MODE ARTICLE (classique)
            if (dto.getParametreId() == null) {
                throw new RuntimeException("L'article et le service sont obligatoires en mode Article");
            }
            if (dto.getQte() == null || dto.getQte() == 0) {
                commande.setQte(1); // Quantité par défaut
            }
            applyParametreEtMontant(commande, dto.getParametreId());
        }

        // Appliquer remise et montant net
        applyRemiseEtNet(commande, dto.getRemise());

        // Statut de la commande
        commande.setStatut(StatutCommande.EN_COURS);

        // Gestion du paiement
        double montantPaye = dto.getMontantPaye() != null ? dto.getMontantPaye() : 0;
        commande.setMontantPaye(montantPaye);

        if (dto.getStatutPaiement() != null) {
            commande.setStatutPaiement(dto.getStatutPaiement());
        } else {
            // Déterminer automatiquement le statut de paiement
            if (montantPaye == 0) {
                commande.setStatutPaiement(StatutPaiement.NON_PAYE);
            } else if (montantPaye >= commande.getMontantNet()) {
                commande.setStatutPaiement(StatutPaiement.PAYE);
            } else {
                commande.setStatutPaiement(StatutPaiement.PARTIELLEMENT_PAYE);
            }
        }

        return commandeRepository.save(commande);
    }

    // ✅ **Nouvelle méthode : Facturation par kilogramme**
    private void applyFacturationKilogramme(Commande commande, CommandeDTO dto) {
        // Validation du poids
        if (dto.getKilo() == null || dto.getKilo() <= 0) {
            throw new RuntimeException("Le poids (en kg) est obligatoire et doit être supérieur à 0");
        }

        // Validation du prix par kg
        if (dto.getPrixParKg() == null || dto.getPrixParKg() <= 0) {
            throw new RuntimeException("Le prix par kg est obligatoire et doit être supérieur à 0");
        }

        // Stocker les informations
        commande.setKilo(dto.getKilo());
        commande.setQte(0); // Pas de quantité en mode kilogramme

        // Stocker le service et le prix par kg
        commande.setServiceKilo(dto.getServiceKilo());
        commande.setPrixParKg(dto.getPrixParKg());
        commande.setTypeFacturation("KILOGRAMME");

        // Calculer le montant brut : poids × prix par kg
        double montantBrut = dto.getKilo() * dto.getPrixParKg();
        commande.setMontantBrut(Math.round(montantBrut * 100.0) / 100.0);

        // ✅ NE PAS lier de paramètre en mode kilogramme
        commande.setParametre(null);
    }

    // 🔹 Appliquer paramètre (MODE ARTICLE)
    private void applyParametreEtMontant(Commande c, Long parametreId) {
        Parametre param = parametreRepository.findById(parametreId)
                .orElseThrow(() -> new RuntimeException("Paramètre introuvable : " + parametreId));

        c.setParametre(param);
        c.setKilo(0.0);
        c.setTypeFacturation("ARTICLE");

        if (c.getQte() == null || c.getQte() == 0) {
            c.setQte(1);
        }

        c.setMontantBrut(param.getPrix() * c.getQte());
    }

    // 🔹 Appliquer remise et net
    private void applyRemiseEtNet(Commande c, Double remise) {
        c.setRemise(remise != null ? remise : 0.0);
        c.setMontantNet(c.getMontantBrut() - c.getRemise());
    }

    // 🔹 Conversion DTO -> Entity
    private Commande fromDto(CommandeDTO dto) {
        Commande c = new Commande();
        c.setQte(dto.getQte() != null ? dto.getQte() : 0);
        c.setKilo(dto.getKilo() != null ? dto.getKilo() : 0.0);
        c.setRemise(dto.getRemise() != null ? dto.getRemise() : 0.0);
        c.setMontantBrut(dto.getMontantBrut() != null ? dto.getMontantBrut() : 0.0);
        c.setMontantNet(dto.getMontantNet() != null ? dto.getMontantNet() : 0.0);
        return c;
    }

    // 🔹 Créer la commande et renvoyer directement le PDF
    public ResponseEntity<byte[]> saveCommandeEtTelechargerPdf(CommandeDTO dto) {
        Commande savedCommande = saveCommandeEntity(dto);

        byte[] pdf = commandePdfService.genererCommandePdf(savedCommande);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=commande_" + savedCommande.getId() + ".pdf")
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

        // ✅ Mise à jour selon le type de facturation
        if (Boolean.TRUE.equals(dto.getTypeKilogramme())) {
            // MODE KILOGRAMME
            if (dto.getKilo() != null && dto.getKilo() > 0) {
                commande.setKilo(dto.getKilo());
            }
            if (dto.getPrixParKg() != null && dto.getPrixParKg() > 0) {
                commande.setPrixParKg(dto.getPrixParKg());
                double montantBrut = commande.getKilo() * dto.getPrixParKg();
                commande.setMontantBrut(Math.round(montantBrut * 100.0) / 100.0);
            }
            if (dto.getServiceKilo() != null) {
                commande.setServiceKilo(dto.getServiceKilo());
            }
            commande.setQte(0);
            commande.setParametre(null);
        } else {
            // MODE ARTICLE
            if (dto.getQte() != null && dto.getQte() > 0) {
                commande.setQte(dto.getQte());
            }
            if (dto.getParametreId() != null) {
                applyParametreEtMontant(commande, dto.getParametreId());
            }
            commande.setKilo(0.0);
            commande.setServiceKilo(null);
            commande.setPrixParKg(null);
        }

        // Appliquer remise et net
        applyRemiseEtNet(commande, dto.getRemise() != null ? dto.getRemise() : commande.getRemise());

        // Mise à jour des dates
        if (dto.getDateReception() != null)
            commande.setDateReception(dto.getDateReception());

        if (dto.getDateLivraison() != null)
            commande.setDateLivraison(dto.getDateLivraison());

        // Mise à jour des statuts
        if (dto.getStatut() != null)
            commande.setStatut(dto.getStatut());

        if (dto.getStatutPaiement() != null)
            commande.setStatutPaiement(dto.getStatutPaiement());

        // Mise à jour du paiement
        if (dto.getMontantPaye() != null) {
            commande.setMontantPaye(dto.getMontantPaye());
        }

        // Sauvegarde
        commandeRepository.save(commande);

        // Conversion en DTO
        CommandeDTO resultat = toDto(commande);

        // Calcul du reste à payer
        if (resultat.getMontantNet() != null && resultat.getMontantPaye() != null) {
            double reste = resultat.getMontantNet() - resultat.getMontantPaye();
            resultat.setResteAPayer(Math.max(reste, 0));
        }

        return resultat;
    }

    // 🔹 Conversion Entity -> DTO
    public CommandeDTO toDto(Commande c) {
        CommandeDTO dto = new CommandeDTO();

        dto.setId(c.getId());

        // --- Client ---
        if (c.getClient() != null) {
            dto.setClientId(c.getClient().getId());
            dto.setClientNom(c.getClient().getNom());
            dto.setClientTelephone(c.getClient().getTelephone());
        }

        // --- Paramètre (uniquement en mode Article) ---
        if (c.getParametre() != null) {
            dto.setParametreId(c.getParametre().getId());
            dto.setArticle(c.getParametre().getArticle());
            dto.setService(c.getParametre().getService());
            dto.setPrix(c.getParametre().getPrix());
        }

        // --- Montants et quantités ---
        dto.setQte(c.getQte());
        dto.setKilo(c.getKilo());
        dto.setMontantBrut(c.getMontantBrut());
        dto.setRemise(c.getRemise());
        dto.setMontantNet(c.getMontantNet());
        dto.setMontantPaye(c.getMontantPaye());
        dto.setResteAPayer(c.getResteAPayer());

        // --- Dates ---
        dto.setDateReception(c.getDateReception());
        dto.setDateLivraison(c.getDateLivraison());

        // --- Statuts ---
        dto.setStatut(c.getStatut());
        dto.setStatutPaiement(c.getStatutPaiement());

        // ✅ Déterminer le type de facturation
        // Si kilo > 0 et qte == 0, c'est une facturation au kilogramme
        boolean isKiloMode = c.getKilo() != null && c.getKilo() > 0 &&
                (c.getQte() == null || c.getQte() == 0);
        dto.setTypeKilogramme(isKiloMode);

        // Si mode kilogramme, récupérer le service et prix par kg
        if (isKiloMode) {
            dto.setServiceKilo(c.getServiceKilo());
            dto.setPrixParKg(c.getPrixParKg());
        }

        return dto;
    }

    // 🔹 Récupération de toutes les commandes
    public List<CommandeDTO> getAllCommandes() {
        Utilisateur user = getUserConnecte();
        return commandeRepository.findAllByPressing(user.getPressing())
                .stream().map(this::toDto).toList();
    }

    // 🔹 Génération PDF
    public ResponseEntity<byte[]> telechargerCommandePdf(Long id) {
        Commande commande = commandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande introuvable : " + id));

        byte[] pdf = commandePdfService.genererCommandePdf(commande);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=commande_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // 🔹 Récupérer une commande par ID
    public CommandeDTO getCommandeById(Long id) {
        Utilisateur user = getUserConnecte();

        Commande commande = commandeRepository
                .findDistinctByIdAndPressingId(id, user.getPressing().getId())
                .orElseThrow(() -> new RuntimeException("Commande introuvable ou accès refusé : " + id));

        return toDto(commande);
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
        Utilisateur user = getUserConnecte();
        return commandeRepository.sumResteAPayerByPressingAndStatutPaiement(
                user.getPressing().getId(),
                List.of(StatutPaiement.NON_PAYE, StatutPaiement.PARTIELLEMENT_PAYE)
        ).orElse(0.0);
    }

    // 🔹 Changer le statut avec paiement
    public CommandeDTO updateStatutCommandeAvecPaiement(Long commandeId,
                                                        StatutCommande nouveauStatut,
                                                        double montantActuel) {
        Utilisateur user = getUserConnecte();

        Commande commande = commandeRepository
                .findDistinctByIdAndPressingId(commandeId, user.getPressing().getId())
                .orElseThrow(() -> new RuntimeException("Commande introuvable ou accès refusé : " + commandeId));

        commande.setStatut(nouveauStatut);

        if (montantActuel > 0) {
            double nouveauMontantPaye = commande.getMontantPaye() + montantActuel;
            commande.setMontantPaye(nouveauMontantPaye);
        }

        Commande saved = commandeRepository.save(commande);
        return toDto(saved);
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
