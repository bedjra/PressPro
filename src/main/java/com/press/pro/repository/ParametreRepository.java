package com.press.pro.repository;

import com.press.pro.Entity.Pressing;
import org.springframework.data.jpa.repository.JpaRepository;
import com.press.pro.Entity.Parametre;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ParametreRepository extends JpaRepository<Parametre, Long> {

    // ✅ Récupération de tous les paramètres d’un pressing
    @Query("SELECT DISTINCT p FROM Parametre p LEFT JOIN FETCH p.pressing WHERE p.pressing = :pressing")
    List<Parametre> findAllByPressing(@Param("pressing") Pressing pressing);

    @Query("SELECT p FROM Parametre p WHERE p.pressing.id = :pressingId")
    List<Parametre> findAllByPressingId(@Param("pressingId") Long pressingId);

    // ✅ Récupération d’un paramètre par id avec pressing
    @Query("SELECT DISTINCT p FROM Parametre p LEFT JOIN FETCH p.pressing WHERE p.id = :id")
    Optional<Parametre> findDistinctByIdWithPressing(@Param("id") Long id);
}
