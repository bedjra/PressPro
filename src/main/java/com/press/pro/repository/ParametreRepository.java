package com.press.pro.repository;

import com.press.pro.Entity.Parametre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ParametreRepository extends JpaRepository<Parametre, Long> {

    // 🔹 Récupérer tous les paramètres d'un pressing
    @Query("SELECT DISTINCT p FROM Parametre p WHERE p.pressing.id = :pressingId")
    List<Parametre> findAllByPressingId(@Param("pressingId") Long pressingId);

    // 🔹 Récupérer un paramètre par ID et pressing (évite doublons)
    @Query("SELECT DISTINCT p FROM Parametre p WHERE p.id = :id")
    Optional<Parametre> findDistinctByIdWithPressing(@Param("id") Long id);
}
