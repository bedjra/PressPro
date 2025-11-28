package com.press.pro.Dto;


public class TarifKiloDto {

    private Long id;
    private String tranchePoids;
    private String service;
    private double prix;

    // --- Getters & Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTranchePoids() {
        return tranchePoids;
    }

    public void setTranchePoids(String tranchePoids) {
        this.tranchePoids = tranchePoids;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }
}
