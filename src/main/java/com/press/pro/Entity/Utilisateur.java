//package com.estock.stock.Entity;
//
//import com.estock.stock.enums.Role;
//import com.fasterxml.jackson.annotation.JsonManagedReference;
//import jakarta.persistence.*;
//import lombok.Data;
//
//
//@Entity
//@Data
//public class Utilisateur {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto-incrément
//    private Long id;
//
//    private String email;
//    private String password;
//
//    @Enumerated(EnumType.STRING)
//    private Role role;
//
//    @OneToOne(mappedBy = "utilisateur", cascade = CascadeType.ALL)
//    @JsonManagedReference
//    private Licence licence;
//
//
//    public long getId() {
//        return id;
//    }
//
//    public void setId(long id) {
//        this.id = id;
//    }
//
//    public String getEmail() {
//        return email;
//    }
//
//    public void setEmail(String email) {
//        this.email = email;
//    }
//
//    public String getPassword() {
//        return password;
//    }
//
//    public void setPassword(String password) {
//        this.password = password;
//    }
//
//    public Role getRole() {
//        return role;
//    }
//
//    public void setRole(Role role) {
//        this.role = role;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public Licence getLicence() {
//        return licence;
//    }
//
//    public void setLicence(Licence licence) {
//        this.licence = licence;
//    }
//}
