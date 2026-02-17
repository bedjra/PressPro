package com.press.pro.repository;


import com.press.pro.Entity.Paiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public interface PaiementRepository extends JpaRepository<Paiement, Long> {

    @Query("""
SELECT COALESCE(SUM(p.montant), 0)
FROM Paiement p
WHERE p.pressing.id = :pressingId
AND DATE(p.datePaiement) = CURRENT_DATE
""")
    BigDecimal sumCAJournalier(@Param("pressingId") Long pressingId);


    @Query("SELECT SUM(p.montant) " +
            "FROM Paiement p " +
            "WHERE p.pressing.id = :pressingId " +
            "AND p.datePaiement BETWEEN :debut AND :fin")
    Optional<Double> sumPaiementsEntreDates(
            @Param("debut") LocalDateTime debut,
            @Param("fin") LocalDateTime fin,
            @Param("pressingId") Long pressingId
    );


}
