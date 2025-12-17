package com.press.pro.repository;


import com.press.pro.Entity.Pressing;
import com.press.pro.enums.StatutCommande;
import org.springframework.data.jpa.repository.JpaRepository;
import com.press.pro.Entity.Commande;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


public interface CommandeRepository extends JpaRepository<Commande, Long> {

    List<Commande> findAllByPressing(Pressing pressing);

    @Query("SELECT DISTINCT c FROM Commande c WHERE c.id = :id AND c.pressing.id = :pressingId")
    Optional<Commande> findDistinctByIdAndPressingId(@Param("id") Long id, @Param("pressingId") Long pressingId);



    // 🔹 Nouvelle méthode pour toutes les commandes d’un pressing
    List<Commande> findAllByPressingId(Long pressingId);

    @Query("SELECT c.pressing.id, COUNT(c) " +
            "FROM Commande c " +
            "WHERE c.pressing.id = :pressingId " +
            "AND FUNCTION('DATE', c.dateReception) = :dateReception " +
            "GROUP BY c.pressing.id")
    List<Object[]> countCommandesByPressingAndDate(
            @Param("pressingId") Long pressingId,
            @Param("dateReception") LocalDate dateReception
    );




    @Query("SELECT c.pressing.id, COUNT(c) " +
            "FROM Commande c " +
            "WHERE c.statut = :statut " +
            "AND c.pressing.id = :pressingId " +
            "AND FUNCTION('DATE', c.dateReception) = CURRENT_DATE " +
            "GROUP BY c.pressing.id")
    List<Object[]> countCommandesEnCoursAujourdHui(
            @Param("pressingId") Long pressingId,
            @Param("statut") StatutCommande statut
    );




    @Query("""
        SELECT c.pressing.id, COUNT(c)
        FROM Commande c
        WHERE c.statut = :statut
        AND c.pressing.id = :pressingId
        AND DATE(c.updatedAt) = :date
        GROUP BY c.pressing.id
        """)
    List<Object[]> countLivrees(
            @Param("statut") StatutCommande statut,
            @Param("pressingId") Long pressingId,
            @Param("date") LocalDate date
    );




    // 🔹 CA total pour un pressing
    @Query("""
    SELECT COALESCE(SUM(c.montantPaye), 0)
    FROM Commande c
    WHERE c.pressing.id = :pressingId
    AND c.montantPaye > 0
""")
    BigDecimal sumChiffreAffairesTotal(@Param("pressingId") Long pressingId);




    // 🔹 CA pour un pressing à une date donnée
    @Query("SELECT SUM(c.montantPaye) FROM Commande c " +
            "WHERE c.pressing.id = :pressingId AND c.dateReception = :dateReception")
    Optional<Double> sumMontantNetByDateAndPressing(@Param("dateReception") LocalDate date,
                                                    @Param("pressingId") Long pressingId);

    // 🔹 CA pour un pressing entre deux dates
    @Query("SELECT SUM(c.montantPaye) FROM Commande c " +
            "WHERE c.pressing.id = :pressingId " +
            "AND c.dateReception BETWEEN :debut AND :fin")
    Optional<Double> sumMontantNetBetweenDatesAndPressing(@Param("debut") LocalDate debut,
                                                          @Param("fin") LocalDate fin,
                                                          @Param("pressingId") Long pressingId);









    @Query("SELECT COALESCE(SUM(c.resteAPayer), 0.0) FROM Commande c WHERE c.pressing.id = :pressingId")
    Double sumResteAPayerByPressing(@Param("pressingId") Long pressingId);



}

