package com.press.pro.Entity;

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

    @Enumerated(EnumType.STRING)
    private Role role; // ⚡ correction : utiliser l'enum Role

    @ManyToOne
    @JoinColumn(name = "pressing_id")
    private Pressing pressing; // Peut être null au début


    public Utilisateur() {}

    public Utilisateur(String email, String password, Role role) {
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setPressing(Pressing pressing) {
        this.pressing = pressing;
    }



    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }

    public Pressing getPressing() {
        return pressing;
    }

}
