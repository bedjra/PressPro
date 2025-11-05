package com.press.pro.Entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.press.pro.enums.Role;
import jakarta.persistence.*;
import lombok.Data;


@Entity
@Data
public class Utilisateur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String password;
    private String role;

    @ManyToOne
    @JoinColumn(name = "pressing_id")
    private Pressing pressing; // Peut être null au début

    public Utilisateur() {}

    public Utilisateur(String email, String password, String role) {
        this.email = email;
        this.password = password;
        this.role = role;
    }


}
