package com.press.pro.Dto;



public class StatutUpdateRequest {

    private Double montantActuel; // obligatoire métier
    private Double reliquat;      // OPTIONNEL

    public Double getMontantActuel() {
        return montantActuel;
    }

    public void setMontantActuel(Double montantActuel) {
        this.montantActuel = montantActuel;
    }

    public Double getReliquat() {
        return reliquat;
    }

    public void setReliquat(Double reliquat) {
        this.reliquat = reliquat;
    }
}
