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

    /**
     * 👉 RELIQUAT SAISI MANUELLEMENT (PAS AUTOMATIQUE)
     */
    @Column(nullable = false)
    private double reliquat;

    private Double montantPayeAujourdHui = 0.0;

    // -----------------------------
    // LOGIQUE METIER
    // -----------------------------

    @Transient
    public double getMontantNetTotal() {
        if (lignes == null) return 0;
        return lignes.stream()
                .mapToDouble(CommandeLigne::getMontantBrut)
                .sum() - remise;
    }

    /**
     * 👉 MÉTHODE EXPLICITE
     * Appelée dans le SERVICE (pas automatiquement)
     */
    public void recalculerPaiement() {
        double totalNet = getMontantNetTotal();
        double totalRegle = montantPaye + reliquat;
        double diff = totalNet - totalRegle;

        if (diff > 0) {
            this.resteAPayer = diff;
            this.statutPaiement = totalRegle == 0
                    ? StatutPaiement.NON_PAYE
                    : StatutPaiement.PARTIELLEMENT_PAYE;
        } else {
            this.resteAPayer = 0;
            this.statutPaiement = StatutPaiement.PAYE;
        }
    }

    // -----------------------------
    // SETTERS SIMPLES (SANS LOGIQUE)
    // -----------------------------

    public void setMontantPaye(double montantPaye) {
        this.montantPaye = montantPaye;
    }

    public void setRemise(double remise) {
        this.remise = remise;
    }

    public void setReliquat(double reliquat) {
        this.reliquat = reliquat;
    }
}
