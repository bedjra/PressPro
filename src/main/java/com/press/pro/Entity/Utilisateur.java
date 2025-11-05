package com.press.pro.Entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.press.pro.enums.Role;
import jakarta.persistence.*;
import lombok.Data;


@Entity
@Data
public class Utilisateur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto-incrément
    private Long id;

    private String email;
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @ManyToOne
    @JoinColumn(name = "pressing_id")
    private Pressing pressing;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Pressing getPressing() {
        return pressing;
    }

    public void setPressing(Pressing pressing) {
        this.pressing = pressing;
    }
}
