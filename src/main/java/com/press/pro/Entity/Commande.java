package com.press.pro.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.press.pro.enums.StatutCommande;
import com.press.pro.enums.StatutPaiement;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
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

    @Column(nullable = false)
    private double resteAPayer;

    @Column(nullable = false)
    private double reliquat;

    // -----------------------------
    // LOGIQUE METIER
    // -----------------------------

    private void updateStatutPaiement() {
        double totalNet = getMontantNetTotal();

        if (montantPaye <= 0) {
            statutPaiement = StatutPaiement.NON_PAYE;
        } else if (montantPaye < totalNet) {
            statutPaiement = StatutPaiement.PARTIELLEMENT_PAYE;
        } else {
            statutPaiement = StatutPaiement.PAYE;
        }
    }

    @Transient
    public double getMontantNetTotal() {
        if (lignes == null) return 0;
        return lignes.stream()
                .mapToDouble(CommandeLigne::getMontantBrut)
                .sum() - remise;
    }


    // Méthode de calcul des soldes
    public void updateSoldes() {
        double totalNet = getMontantNetTotal();
        double diff = totalNet - montantPaye;

        if (diff > 0) {
            this.resteAPayer = diff;
            this.reliquat = 0;
        } else {
            this.resteAPayer = 0;
            this.reliquat = Math.abs(diff);
        }
    }


    // -----------------------------
    // SETTERS PERSONNALISÉS
    // -----------------------------

    public void setMontantPaye(double montantPaye) {
        this.montantPaye = montantPaye;
        updateStatutPaiement();
        updateSoldes();
    }

    public void setRemise(double remise) {
        this.remise = remise;
        updateStatutPaiement();
        updateSoldes();
    }
}
