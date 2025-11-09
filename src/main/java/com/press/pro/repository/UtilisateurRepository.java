package com.press.pro.repository;

import com.press.pro.Entity.Pressing;
import com.press.pro.Entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    @Query("SELECT DISTINCT u FROM Utilisateur u LEFT JOIN FETCH u.pressing WHERE u.email = :email")
    Optional<Utilisateur> findDistinctByEmailWithPressing(@Param("email") String email);

    @Query("SELECT DISTINCT u FROM Utilisateur u LEFT JOIN FETCH u.pressing WHERE u.pressing = :pressing")
    List<Utilisateur> findAllByPressing(@Param("pressing") Pressing pressing);


    @Query("SELECT DISTINCT u FROM Utilisateur u LEFT JOIN FETCH u.pressing WHERE u.email = :email")
    Optional<Utilisateur> findByEmail(@Param("email") String email);
}

