package com.press.pro.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.press.pro.enums.StatutCommande;
import com.press.pro.enums.StatutPaiement;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class Commande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    // ✅ IMPORTANT : nullable = true pour permettre le mode kilogramme
    @ManyToOne
    @JoinColumn(name = "parametre_id", nullable = true)
    private Parametre parametre;

    @ManyToOne
    @JoinColumn(name = "pressing_id", nullable = false)
    private Pressing pressing;

    private Integer qte;
    private Double kilo;
    private Double montantBrut;
    private Double remise;
    private Double montantNet;

    @Enumerated(EnumType.STRING)
    private StatutCommande statut;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateReception;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateLivraison;

    @Enumerated(EnumType.STRING)
    private StatutPaiement statutPaiement = StatutPaiement.NON_PAYE;

    @Column(nullable = false)
    private Double montantPaye = 0.0;

    // ✅ Champs pour le mode kilogramme
    @Column(name = "service_kilo")
    private String serviceKilo;

    @Column(name = "prix_par_kg")
    private Double prixParKg;

    @Column(name = "type_facturation")
    private String typeFacturation; // "ARTICLE" ou "KILOGRAMME"

    // Méthodes
    public void setMontantPaye(Double montantPaye) {
        this.montantPaye = montantPaye != null ? montantPaye : 0.0;
        updateStatutPaiement();
    }

    private void updateStatutPaiement() {
        if (montantPaye <= 0) {
            this.statutPaiement = StatutPaiement.NON_PAYE;
        } else if (montantPaye < this.montantNet) {
            this.statutPaiement = StatutPaiement.PARTIELLEMENT_PAYE;
        } else {
            this.statutPaiement = StatutPaiement.PAYE;
        }
    }

    @Transient
    public Double getResteAPayer() {
        double reste = (this.montantNet != null ? this.montantNet : 0.0) -
                (this.montantPaye != null ? this.montantPaye : 0.0);
        return Math.max(reste, 0.0);
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Parametre getParametre() {
        return parametre;
    }

    public void setParametre(Parametre parametre) {
        this.parametre = parametre;
    }

    public Pressing getPressing() {
        return pressing;
    }

    public void setPressing(Pressing pressing) {
        this.pressing = pressing;
    }

    public Integer getQte() {
        return qte;
    }

    public void setQte(Integer qte) {
        this.qte = qte;
    }

    public Double getKilo() {
        return kilo;
    }

    public void setKilo(Double kilo) {
        this.kilo = kilo;
    }

    public Double getMontantBrut() {
        return montantBrut;
    }

    public void setMontantBrut(Double montantBrut) {
        this.montantBrut = montantBrut;
    }

    public Double getRemise() {
        return remise;
    }

    public void setRemise(Double remise) {
        this.remise = remise;
    }

    public Double getMontantNet() {
        return montantNet;
    }

    public void setMontantNet(Double montantNet) {
        this.montantNet = montantNet;
    }

    public StatutCommande getStatut() {
        return statut;
    }

    public void setStatut(StatutCommande statut) {
        this.statut = statut;
    }

    public LocalDate getDateReception() {
        return dateReception;
    }

    public void setDateReception(LocalDate dateReception) {
        this.dateReception = dateReception;
    }

    public LocalDate getDateLivraison() {
        return dateLivraison;
    }

    public void setDateLivraison(LocalDate dateLivraison) {
        this.dateLivraison = dateLivraison;
    }

    public StatutPaiement getStatutPaiement() {
        return statutPaiement;
    }

    public void setStatutPaiement(StatutPaiement statutPaiement) {
        this.statutPaiement = statutPaiement;
    }

    public Double getMontantPaye() {
        return montantPaye;
    }

    public String getServiceKilo() {
        return serviceKilo;
    }

    public void setServiceKilo(String serviceKilo) {
        this.serviceKilo = serviceKilo;
    }

    public Double getPrixParKg() {
        return prixParKg;
    }

    public void setPrixParKg(Double prixParKg) {
        this.prixParKg = prixParKg;
    }

    public String getTypeFacturation() {
        return typeFacturation;
    }

    public void setTypeFacturation(String typeFacturation) {
        this.typeFacturation = typeFacturation;
    }
}