package com.press.pro.repository;

import com.press.pro.Entity.Charge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.math.BigDecimal;

@Repository
public interface ChargeRepository extends JpaRepository<Charge, Long> {


    @Query("SELECT DISTINCT c FROM Charge c WHERE c.pressing.id = :pressingId")
    List<Charge> findDistinctByPressingId(@Param("pressingId") Long pressingId);


    // ✅ Filtrage par dates pour le mois
    @Query("""
    SELECT c FROM Charge c
    WHERE c.pressing.id = :pressingId
    AND c.dateCharge BETWEEN :start AND :end
""")
    List<Charge> findChargesBetweenDatesAndPressing(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            @Param("pressingId") Long pressingId
    );




    // ✅ Total des charges d’une journée
    @Query("""
        SELECT COALESCE(SUM(c.montant), 0)
        FROM Charge c
        WHERE c.dateCharge = :date
          AND c.pressing.id = :pressingId
    """)
    Double sumChargesByDateAndPressing(
            @Param("date") LocalDate date,
            @Param("pressingId") Long pressingId
    );

    @Query("SELECT COALESCE(SUM(c.montant), 0) FROM Charge c WHERE c.pressing.id = :pressingId")
    BigDecimal sumByPressingId(@Param("pressingId") Long pressingId);



    // 🔹 Charges d’un mois précis POUR UN PRESSING
    @Query("""
    SELECT COALESCE(SUM(c.montant), 0)
    FROM Charge c
    WHERE c.dateCharge BETWEEN :start AND :end
      AND c.pressing.id = :pressingId
""")
    BigDecimal sumChargesBetweenDatesAndPressing(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            @Param("pressingId") Long pressingId
    );


    // 🔹 Charges d’une année POUR UN PRESSING
    @Query("""
        SELECT c
        FROM Charge c
        WHERE YEAR(c.dateCharge) = :annee
          AND c.pressing.id = :pressingId
    """)
    List<Charge> findChargesByYearAndPressing(
            @Param("annee") int annee,
            @Param("pressingId") Long pressingId
    );




/// repartition des charges mois pR MOIS
    @Query("""
        SELECT COALESCE(SUM(c.montant), 0)
        FROM Charge c
        WHERE MONTH(c.dateCharge) = MONTH(CURRENT_DATE)
          AND YEAR(c.dateCharge) = YEAR(CURRENT_DATE)
    """)
    BigDecimal getTotalMoisCourant();

    @Query("""
        SELECT c
        FROM Charge c
        WHERE MONTH(c.dateCharge) = MONTH(CURRENT_DATE)
          AND YEAR(c.dateCharge) = YEAR(CURRENT_DATE)
        ORDER BY c.dateCharge DESC
    """)
    List<Charge> findMoisCourant();
}