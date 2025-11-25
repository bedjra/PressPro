package com.press.pro.Dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.press.pro.enums.StatutCommande;
import com.press.pro.enums.StatutPaiement;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public class CommandeDTO {

    private Long id;

    // Client simplifié : nom, téléphone et ID pour liaison
    private Long clientId;
    private String clientNom;
    private String clientTelephone;

    // Paramètre complet + ID pour liaison
    private Long parametreId;

    // Liste d'articles/services/prix
    private List<String> articles;
    private List<String> services;
    private List<Double> prix;
    // private List<Double> kilos; // si besoin

    // Quantités et montants correspondants
    private List<Integer> qtes;
    private List<Double> montantsBruts;
    private List<Double> remises;
    private List<Double> montantsNets;

    // Montants payés et reste à payer
    private Double montantPaye;    // montant déjà versé
    private Double resteAPayer;    // calculé = montantNet - montantPaye

    @NotNull(message = "La date de réception est obligatoire")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateReception;

    @NotNull(message = "La date de livraison est obligatoire")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateLivraison;

    // Statuts
    private StatutCommande statut;
    private StatutPaiement statutPaiement;

    // Getters et setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public String getClientNom() { return clientNom; }
    public void setClientNom(String clientNom) { this.clientNom = clientNom; }

    public String getClientTelephone() { return clientTelephone; }
    public void setClientTelephone(String clientTelephone) { this.clientTelephone = clientTelephone; }

    public Long getParametreId() { return parametreId; }
    public void setParametreId(Long parametreId) { this.parametreId = parametreId; }

    public List<String> getArticles() { return articles; }
    public void setArticles(List<String> articles) { this.articles = articles; }

    public List<String> getServices() { return services; }
    public void setServices(List<String> services) { this.services = services; }

    public List<Double> getPrix() { return prix; }
    public void setPrix(List<Double> prix) { this.prix = prix; }

    public List<Integer> getQtes() { return qtes; }
    public void setQtes(List<Integer> qtes) { this.qtes = qtes; }

    public List<Double> getMontantsBruts() { return montantsBruts; }
    public void setMontantsBruts(List<Double> montantsBruts) { this.montantsBruts = montantsBruts; }

    public List<Double> getRemises() { return remises; }
    public void setRemises(List<Double> remises) { this.remises = remises; }

    public List<Double> getMontantsNets() { return montantsNets; }
    public void setMontantsNets(List<Double> montantsNets) { this.montantsNets = montantsNets; }

    public Double getMontantPaye() { return montantPaye; }
    public void setMontantPaye(Double montantPaye) { this.montantPaye = montantPaye; }

    public Double getResteAPayer() { return resteAPayer; }
    public void setResteAPayer(Double resteAPayer) { this.resteAPayer = resteAPayer; }

    public LocalDate getDateReception() { return dateReception; }
    public void setDateReception(LocalDate dateReception) { this.dateReception = dateReception; }

    public LocalDate getDateLivraison() { return dateLivraison; }
    public void setDateLivraison(LocalDate dateLivraison) { this.dateLivraison = dateLivraison; }

    public StatutCommande getStatut() { return statut; }
    public void setStatut(StatutCommande statut) { this.statut = statut; }

    public StatutPaiement getStatutPaiement() { return statutPaiement; }
    public void setStatutPaiement(StatutPaiement statutPaiement) { this.statutPaiement = statutPaiement; }
}
