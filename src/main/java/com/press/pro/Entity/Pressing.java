package com.press.pro.Entity;

import jakarta.persistence.*;


@Entity
public class Pressing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String email;
    private String telephone;
    private String adresse;

//    // constructeur vide
//    public Pressing() {}

//    // constructeur complet
//    public Pressing(String nom, String email, String telephone, String adresse) {
//        this.nom = nom;
//        this.email = email;
//        this.telephone = telephone;
//        this.adresse = adresse;
//    }


}
