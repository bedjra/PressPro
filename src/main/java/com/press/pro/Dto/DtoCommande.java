package com.press.pro.Dto;

import com.press.pro.enums.StatutCommande;
import com.press.pro.enums.StatutPaiement;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data   // ✅ getters / setters / toString / equals / hashCode
public class DtoCommande {

    private Long id;

    // -------------------------------
    // Client
    // -------------------------------
    private Long clientId;
    private String clientNom;

    // -------------------------------
    // Pressing
    // -------------------------------
    private Long pressingId;

    // -------------------------------
    // LIGNES : ARTICLE OU KILO
    // -------------------------------

    // 🔹 Cas 1 : Par article
    private List<Long> parametreIds;
    private List<Integer> quantites;

    // 🔹 Cas 2 : Par kilo
    private List<Long> tarifKiloIds;
    private List<Double> poids;

    // -------------------------------
    // Remise & paiement
    // -------------------------------
    private Double remiseGlobale;
    private Double montantPaye;
    private Double resteAPayer;   // ✅ déjà existant
    private Double reliquat;      // ✅ AJOUT ICI

    private StatutPaiement statutPaiement;
    private StatutCommande statut;

    // -------------------------------
    // Dates
    // -------------------------------
    private LocalDate dateReception;
    private LocalDate dateLivraison;

    // -------------------------------
    // Infos retour frontend
    // -------------------------------
    private List<String> articles;
    private List<String> services;
    private List<Double> prixUnitaires;
    private List<Double> montantsBruts;
    private List<Double> montantsNets;
}
