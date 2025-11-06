package com.press.pro.repository;

import com.press.pro.Entity.Pressing;
import com.press.pro.Entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    Optional<Utilisateur> findByEmail(String email);


    List<Utilisateur> findByPressing(Pressing pressing);
}

