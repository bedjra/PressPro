package com.press.pro.Entity;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Paiement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "commande_id", nullable = false)
    private Commande commande;

    @ManyToOne
    @JoinColumn(name = "pressing_id", nullable = false)
    private Pressing pressing;

    @Column(nullable = false)
    private double montant;

    private LocalDateTime datePaiement;

    @PrePersist
    public void prePersist() {
        this.datePaiement = LocalDateTime.now();
    }
}
