package com.press.pro.Entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
public class Pressing {
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;
    @Lob
    private byte[] logo;
    private String nom;
    private String email;
    private String telephone;
    private String cel;
    private String adresse;
    @CreationTimestamp
    @Column(
            updatable = false
    )
    private LocalDateTime dateCreation;
    @Column(
            nullable = false
    )
    private boolean actif = true;
    @OneToOne(
            mappedBy = "pressing",
            fetch = FetchType.LAZY
    )
    private Utilisateur admin;
    @OneToMany(
            mappedBy = "pressing",
            cascade = {CascadeType.ALL},
            fetch = FetchType.LAZY
    )
    private List<Commande> commandes = new ArrayList();
    @OneToMany(
            mappedBy = "pressing",
            cascade = {CascadeType.ALL},
            fetch = FetchType.LAZY
    )
    private List<Utilisateur> utilisateurs = new ArrayList();
    @OneToMany(
            mappedBy = "pressing",
            cascade = {CascadeType.ALL},
            fetch = FetchType.LAZY
    )
    private List<Client> clients = new ArrayList();
    @OneToMany(
            mappedBy = "pressing",
            cascade = {CascadeType.ALL},
            fetch = FetchType.LAZY
    )
    private List<Charge> charges = new ArrayList();
    @OneToMany(
            mappedBy = "pressing",
            cascade = {CascadeType.ALL},
            fetch = FetchType.LAZY
    )
    private List<CommandeLigne> commandeLignes = new ArrayList();
    @OneToMany(
            mappedBy = "pressing",
            cascade = {CascadeType.ALL},
            fetch = FetchType.LAZY
    )
    private List<TarifKilo> tarifKilos = new ArrayList();
    @OneToMany(
            mappedBy = "pressing",
            cascade = {CascadeType.ALL},
            fetch = FetchType.LAZY
    )
    private List<Parametre> parametres = new ArrayList();

    public List<Commande> getCommandes() {
        return this.commandes;
    }

    public void setCommandes(List<Commande> commandes) {
        this.commandes = commandes;
    }

    public Pressing() {
    }

    public Pressing(String nom, byte[] logo, String email, String telephone, String cel, String adresse) {
        this.nom = nom;
        this.logo = logo;
        this.email = email;
        this.telephone = telephone;
        this.cel = cel;
        this.adresse = adresse;
    }

    public String getCel() {
        return this.cel;
    }

    public void setCel(String cel) {
        this.cel = cel;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public byte[] getLogo() {
        return this.logo;
    }

    public void setLogo(byte[] logo) {
        this.logo = logo;
    }

    public String getNom() {
        return this.nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return this.telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getAdresse() {
        return this.adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public Utilisateur getAdmin() {
        return this.admin;
    }

    public void setAdmin(Utilisateur admin) {
        this.admin = admin;
    }

    public boolean isActif() {
        return this.actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public LocalDateTime getDateCreation() {
        return this.dateCreation;
    }
}
