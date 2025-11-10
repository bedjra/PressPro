package com.press.pro.Dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.press.pro.enums.StatutCommande;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class CommandeDTO {

    private Long id;

    // Client simplifié : nom, téléphone et ID pour liaison
    private Long clientId;
    private String clientNom;
    private String clientTelephone;

    // Paramètre complet + ID pour liaison
    private Long parametreId;
    private String article;
    private String service;
    private Double prix;

    // Changement de int -> Integer et double -> Double pour permettre null
    private Integer qte;
    private Double montantBrut;
    private Double remise;
    private Double montantNet;
    private boolean express;


    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateReception;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateLivraison;



    private StatutCommande statut;

    // Getters et Setters


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

    public String getClientTelephone() {
        return clientTelephone;
    }

    public void setClientTelephone(String clientTelephone) {
        this.clientTelephone = clientTelephone;
    }

    public Long getParametreId() {
        return parametreId;
    }

    public void setParametreId(Long parametreId) {
        this.parametreId = parametreId;
    }

    public String getArticle() {
        return article;
    }

    public void setArticle(String article) {
        this.article = article;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public Double getPrix() {
        return prix;
    }

    public void setPrix(Double prix) {
        this.prix = prix;
    }

    public Integer getQte() {
        return qte;
    }

    public void setQte(Integer qte) {
        this.qte = qte;
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

    public boolean isExpress() {
        return express;
    }

    public void setExpress(boolean express) {
        this.express = express;
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

    public StatutCommande getStatut() {
        return statut;
    }

    public void setStatut(StatutCommande statut) {
        this.statut = statut;
    }
}