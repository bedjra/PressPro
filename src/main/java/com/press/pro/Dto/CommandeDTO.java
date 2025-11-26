package com.press.pro.Dto;

import com.press.pro.enums.StatutCommande;
import com.press.pro.enums.StatutPaiement;

import java.time.LocalDate;
import java.util.List;

public class CommandeDTO {

    private Long id;
    private Long clientId;
    private String clientNom; // <-- nouveau champ

    private List<Long> parametreIds; // <- liste de parametreId
    private Integer qte;              // <- quantité globale
    private Double remiseGlobale;
    private Double montantPaye;
    private LocalDate dateReception;
    private LocalDate dateLivraison;

    private StatutPaiement statutPaiement;

    private StatutCommande statut; // <-- ajouté ici


    // Pour retourner les infos détaillées côté frontend
    private List<String> articles;
    private List<String> services;
    private List<Double> prix;
    private List<Integer> qtes;
    private List<Double> montantsBruts;
    private List<Double> montantsNets;

    // getters & setters pour **toutes les propriétés**

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public StatutPaiement getStatutPaiement() {
        return statutPaiement;
    }

    public void setStatutPaiement(StatutPaiement statutPaiement) {
        this.statutPaiement = statutPaiement;
    }

    public String getClientNom() {
        return clientNom;
    }

    public void setClientNom(String clientNom) {
        this.clientNom = clientNom;
    }

    public StatutCommande getStatut() { return statut; }
    public void setStatut(StatutCommande statut) { this.statut = statut; }



    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public List<Long> getParametreIds() {
        return parametreIds;
    }

    public void setParametreIds(List<Long> parametreIds) {
        this.parametreIds = parametreIds;
    }

    public Integer getQte() {
        return qte;
    }

    public void setQte(Integer qte) {
        this.qte = qte;
    }

    public Double getRemiseGlobale() {
        return remiseGlobale;
    }

    public void setRemiseGlobale(Double remiseGlobale) {
        this.remiseGlobale = remiseGlobale;
    }

    public Double getMontantPaye() {
        return montantPaye;
    }

    public void setMontantPaye(Double montantPaye) {
        this.montantPaye = montantPaye;
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

    public List<String> getArticles() {
        return articles;
    }

    public void setArticles(List<String> articles) {
        this.articles = articles;
    }

    public List<String> getServices() {
        return services;
    }

    public void setServices(List<String> services) {
        this.services = services;
    }

    public List<Double> getPrix() {
        return prix;
    }

    public void setPrix(List<Double> prix) {
        this.prix = prix;
    }

    public List<Integer> getQtes() {
        return qtes;
    }

    public void setQtes(List<Integer> qtes) {
        this.qtes = qtes;
    }

    public List<Double> getMontantsBruts() {
        return montantsBruts;
    }

    public void setMontantsBruts(List<Double> montantsBruts) {
        this.montantsBruts = montantsBruts;
    }

    public List<Double> getMontantsNets() {
        return montantsNets;
    }

    public void setMontantsNets(List<Double> montantsNets) {
        this.montantsNets = montantsNets;
    }
}
