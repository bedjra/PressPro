package com.press.pro.Dto;

import com.press.pro.enums.StatutCommande;

public class StatutUpdateRequest {
    private StatutCommande nouveauStatut;
    private double montantActuel;

    // Getters & setters
    public StatutCommande getNouveauStatut() { return nouveauStatut; }
    public void setNouveauStatut(StatutCommande nouveauStatut) { this.nouveauStatut = nouveauStatut; }

    public double getMontantActuel() { return montantActuel; }
    public void setMontantActuel(double montantActuel) { this.montantActuel = montantActuel; }
}
