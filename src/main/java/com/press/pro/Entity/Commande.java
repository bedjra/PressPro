//package com.press.pro.Entity;
//
//import jakarta.persistence.*;
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "commandes")
//public class Commande {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    // 🔗 Relation avec Client
//    @ManyToOne
//    @JoinColumn(name = "client_id", nullable = false)
//    private Client client;
//
//    // 🔗 Relation avec Parametre (article et service)
//    @ManyToOne
//    @JoinColumn(name = "parametre_id", nullable = false)
//    private Parametre parametre;
//
//    private int qte;
//
//    private LocalDateTime date;
//
//    @PrePersist
//    protected void onCreate() {
//        this.date = LocalDateTime.now();
//    }
//
//    // ----- Getters / Setters -----
//
//    public Long getId() { return id; }
//    public void setId(Long id) { this.id = id; }
//
//    public Client getClient() { return client; }
//    public void setClient(Client client) { this.client = client; }
//
//    public Parametre getParametre() { return parametre; }
//    public void setParametre(Parametre parametre) { this.parametre = parametre; }
//
//    public int getQte() { return qte; }
//    public void setQte(int qte) { this.qte = qte; }
//
//    public LocalDateTime getDate() { return date; }
//}
