package com.press.pro.Dto;

public class CAMoisDTO {

    private Integer annee;
    private Integer mois;
    private Double total;

    public CAMoisDTO(Integer annee, Integer mois, Double total) {
        this.annee = annee;
        this.mois = mois;
        this.total = total;
    }

    public Integer getAnnee() { return annee; }
    public Integer getMois() { return mois; }
    public Double getTotal() { return total; }
}