package com.press.pro.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.press.pro.enums.StatutCommande;
import com.press.pro.enums.StatutPaiement;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Builder
@Data
public class Commande {
        @Id
        @GeneratedValue(
                strategy = GenerationType.IDENTITY
        )
        private Long id;
        @ManyToOne
        @JoinColumn(
                name = "client_id",
                nullable = false
        )
        private Client client;
        @ManyToOne
        @JoinColumn(
                name = "pressing_id",
                nullable = false
        )
        private Pressing pressing;
        @OneToMany(
                mappedBy = "commande",
                cascade = {CascadeType.ALL},
                orphanRemoval = true
        )
        private List<CommandeLigne> lignes;
        @Enumerated(EnumType.STRING)
        private StatutCommande statut;
        @JsonFormat(
                pattern = "yyyy-MM-dd"
        )
        private LocalDate dateReception;
        @JsonFormat(
                pattern = "yyyy-MM-dd"
        )
        private LocalDate dateLivraison;
        @UpdateTimestamp
        private LocalDateTime updatedAt;
        @Enumerated(EnumType.STRING)
        private StatutPaiement statutPaiement;
        @Column(
                nullable = false
        )

        private double montantPaye;
        @Column(
                nullable = false
        )
        private double remise;
        @Column(
                nullable = false
        )
        private double resteAPayer;
        @Column(
                nullable = false
        )
        private double reliquat;
        private Double montantPayeAujourdHui;
        private Double montantPayeSemaine;

        @Transient
        public double getMontantNetTotal() {
            return this.lignes == null ? (double)0.0F : this.lignes.stream().mapToDouble(CommandeLigne::getMontantBrut).sum() - this.remise;
        }

        public void recalculerPaiement() {
            double totalNet = this.getMontantNetTotal();
            double totalRegle = this.montantPaye + this.reliquat;
            double diff = totalNet - totalRegle;
            if (diff > (double)0.0F) {
                this.resteAPayer = diff;
                this.statutPaiement = totalRegle == (double)0.0F ? StatutPaiement.NON_PAYE : StatutPaiement.PARTIELLEMENT_PAYE;
            } else {
                this.resteAPayer = (double)0.0F;
                this.statutPaiement = StatutPaiement.PAYE;
            }

        }

        public void setMontantPaye(double montantPaye) {
            this.montantPaye = montantPaye;
        }

        public void setRemise(double remise) {
            this.remise = remise;
        }

        public void setReliquat(double reliquat) {
            this.reliquat = reliquat;
        }

        public static CommandeBuilder builder() {
            return new CommandeBuilder();
        }

        public Commande() {
            this.statutPaiement = StatutPaiement.NON_PAYE;
            this.montantPayeAujourdHui = (double)0.0F;
            this.montantPayeSemaine = (double)0.0F;
        }

        public Commande(final Long id, final Client client, final Pressing pressing, final List<CommandeLigne> lignes, final StatutCommande statut, final LocalDate dateReception, final LocalDate dateLivraison, final LocalDateTime updatedAt, final StatutPaiement statutPaiement, final double montantPaye, final double remise, final double resteAPayer, final double reliquat, final Double montantPayeAujourdHui, final Double montantPayeSemaine) {
            this.statutPaiement = StatutPaiement.NON_PAYE;
            this.montantPayeAujourdHui = (double)0.0F;
            this.montantPayeSemaine = (double)0.0F;
            this.id = id;
            this.client = client;
            this.pressing = pressing;
            this.lignes = lignes;
            this.statut = statut;
            this.dateReception = dateReception;
            this.dateLivraison = dateLivraison;
            this.updatedAt = updatedAt;
            this.statutPaiement = statutPaiement;
            this.montantPaye = montantPaye;
            this.remise = remise;
            this.resteAPayer = resteAPayer;
            this.reliquat = reliquat;
            this.montantPayeAujourdHui = montantPayeAujourdHui;
            this.montantPayeSemaine = montantPayeSemaine;
        }

        public Long getId() {
            return this.id;
        }

        public Client getClient() {
            return this.client;
        }

        public Pressing getPressing() {
            return this.pressing;
        }

        public List<CommandeLigne> getLignes() {
            return this.lignes;
        }

        public StatutCommande getStatut() {
            return this.statut;
        }

        public LocalDate getDateReception() {
            return this.dateReception;
        }

        public LocalDate getDateLivraison() {
            return this.dateLivraison;
        }

        public LocalDateTime getUpdatedAt() {
            return this.updatedAt;
        }

        public StatutPaiement getStatutPaiement() {
            return this.statutPaiement;
        }

        public double getMontantPaye() {
            return this.montantPaye;
        }

        public double getRemise() {
            return this.remise;
        }

        public double getResteAPayer() {
            return this.resteAPayer;
        }

        public double getReliquat() {
            return this.reliquat;
        }

        public Double getMontantPayeAujourdHui() {
            return this.montantPayeAujourdHui;
        }

        public Double getMontantPayeSemaine() {
            return this.montantPayeSemaine;
        }

        public void setId(final Long id) {
            this.id = id;
        }

        public void setClient(final Client client) {
            this.client = client;
        }

        public void setPressing(final Pressing pressing) {
            this.pressing = pressing;
        }

        public void setLignes(final List<CommandeLigne> lignes) {
            this.lignes = lignes;
        }

        public void setStatut(final StatutCommande statut) {
            this.statut = statut;
        }

        @JsonFormat(
                pattern = "yyyy-MM-dd"
        )
        public void setDateReception(final LocalDate dateReception) {
            this.dateReception = dateReception;
        }

        @JsonFormat(
                pattern = "yyyy-MM-dd"
        )
        public void setDateLivraison(final LocalDate dateLivraison) {
            this.dateLivraison = dateLivraison;
        }

        public void setUpdatedAt(final LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
        }

        public void setStatutPaiement(final StatutPaiement statutPaiement) {
            this.statutPaiement = statutPaiement;
        }

        public void setResteAPayer(final double resteAPayer) {
            this.resteAPayer = resteAPayer;
        }

        public void setMontantPayeAujourdHui(final Double montantPayeAujourdHui) {
            this.montantPayeAujourdHui = montantPayeAujourdHui;
        }

        public void setMontantPayeSemaine(final Double montantPayeSemaine) {
            this.montantPayeSemaine = montantPayeSemaine;
        }
    }
