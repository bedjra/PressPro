package com.press.pro.Dto;

import com.press.pro.enums.StatutCommande;
import com.press.pro.enums.StatutPaiement;

import java.time.LocalDate;
import java.util.List;

public class DtoCommande {

    private Long id;

    // --- Client ---
    private Long clientId;
    private String clientNom;

    // --- Pressing ---
    private Long pressingId;

    // -------------------------------
    //  LIGNES : ARTICLE OU KILO
    // -------------------------------

    // 🔹 Cas 1 : Par article
    // Exemple : chemise, pantalon, costume...
    private List<Long> parametreIds;
    private List<Integer> quantites;   // même taille que parametreIds

    // 🔹 Cas 2 : Par kilo
    // Exemple : 1-4Kg, 5-9Kg…
    private List<Long> tarifKiloIds;
    private List<Double> poids;        // même taille que tarifKiloIds

    // -------------------------------
    // Remise et paiement
    // -------------------------------
    private Double remiseGlobale;
    private Double montantPaye;
    private StatutPaiement statutPaiement;
    private StatutCommande statut;

    // -------------------------------
    // Dates
    // -------------------------------
    private LocalDate dateReception;
    private LocalDate dateLivraison;

    // -------------------------------
    // Infos de retour au frontend
    // -------------------------------
    private List<String> articles;       // noms d’articles ou tranches de poids
    private List<String> services;       // service associé
    private List<Double> prixUnitaires;
    private List<Double> montantsBruts;
    private List<Double> montantsNets;


    // -------------------------------
// Reste à payer
// -------------------------------
    private Double resteAPayer;

    public Double getResteAPayer() {
        return resteAPayer;
    }

    public void setResteAPayer(Double resteAPayer) {
        this.resteAPayer = resteAPayer;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public String getClientNom() {
        return clientNom;
    }

    public void setClientNom(String clientNom) {
        this.clientNom = clientNom;
    }

    public Long getPressingId() {
        return pressingId;
    }

    public void setPressingId(Long pressingId) {
        this.pressingId = pressingId;
    }

    public List<Long> getParametreIds() {
        return parametreIds;
    }

    public void setParametreIds(List<Long> parametreIds) {
        this.parametreIds = parametreIds;
    }

    public List<Integer> getQuantites() {
        return quantites;
    }

    public void setQuantites(List<Integer> quantites) {
        this.quantites = quantites;
    }

    public List<Long> getTarifKiloIds() {
        return tarifKiloIds;
    }

    public void setTarifKiloIds(List<Long> tarifKiloIds) {
        this.tarifKiloIds = tarifKiloIds;
    }

    public List<Double> getPoids() {
        return poids;
    }

    public void setPoids(List<Double> poids) {
        this.poids = poids;
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

    public StatutPaiement getStatutPaiement() {
        return statutPaiement;
    }

    public void setStatutPaiement(StatutPaiement statutPaiement) {
        this.statutPaiement = statutPaiement;
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

    public List<Double> getPrixUnitaires() {
        return prixUnitaires;
    }

    public void setPrixUnitaires(List<Double> prixUnitaires) {
        this.prixUnitaires = prixUnitaires;
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
