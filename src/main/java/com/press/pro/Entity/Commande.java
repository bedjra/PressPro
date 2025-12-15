package com.press.pro.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.press.pro.enums.StatutCommande;
import com.press.pro.enums.StatutPaiement;
import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Commande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne
    @JoinColumn(name = "pressing_id", nullable = false)
    private Pressing pressing;

    // Lignes de commande
    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommandeLigne> lignes;

    @Enumerated(EnumType.STRING)
    private StatutCommande statut;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateReception;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateLivraison;

    @UpdateTimestamp
    private LocalDateTime updatedAt;


    @Enumerated(EnumType.STRING)
    private StatutPaiement statutPaiement = StatutPaiement.NON_PAYE;

    @Column(nullable = false)
    private double montantPaye;

    @Column(nullable = false)
    private double remise;


    // -----------------------------
    // LOGIQUE PAIEMENT
    // -----------------------------


    private void updateStatutPaiement() {
        double totalNet = getMontantNetTotal();

        if (montantPaye <= 0) {
            this.statutPaiement = StatutPaiement.NON_PAYE;
        } else if (montantPaye < totalNet) {
            this.statutPaiement = StatutPaiement.PARTIELLEMENT_PAYE;
        } else {
            this.statutPaiement = StatutPaiement.PAYE;
        }
    }

    @Transient
    public double getMontantNetTotal() {
        if (lignes == null) return 0;
        double total = lignes.stream()
                .mapToDouble(CommandeLigne::getMontantBrut)
                .sum();
        return total - remise; // appliquer la remise globale
    }

    @Transient
    public double getResteAPayer() {
        return getMontantNetTotal() - this.montantPaye;
    }

    // -----------------------------
    // GETTERS & SETTERS
    // -----------------------------
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }

    public Pressing getPressing() { return pressing; }
    public void setPressing(Pressing pressing) { this.pressing = pressing; }

    public List<CommandeLigne> getLignes() { return lignes; }
    public void setLignes(List<CommandeLigne> lignes) { this.lignes = lignes; }

    public StatutCommande getStatut() { return statut; }
    public void setStatut(StatutCommande statut) { this.statut = statut; }

    public LocalDate getDateReception() { return dateReception; }
    public void setDateReception(LocalDate dateReception) { this.dateReception = dateReception; }

    public LocalDate getDateLivraison() { return dateLivraison; }
    public void setDateLivraison(LocalDate dateLivraison) { this.dateLivraison = dateLivraison; }

    public StatutPaiement getStatutPaiement() { return statutPaiement; }
    public void setStatutPaiement(StatutPaiement statutPaiement) { this.statutPaiement = statutPaiement; }

    public double getMontantPaye() { return montantPaye; }

    public double getRemise() { return remise; }
    public void setRemise(double remise) { this.remise = remise; }

    @Column(nullable = false)
    private double resteAPayer;

    public void setMontantPaye(double montantPaye) {
        this.montantPaye = montantPaye;
        updateStatutPaiement();
        updateResteAPayer();
    }

    private void updateResteAPayer() {
        this.resteAPayer = getMontantNetTotal() - this.montantPaye;
    }

}
