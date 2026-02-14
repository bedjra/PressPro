package com.press.pro.repository;


import com.press.pro.Entity.Pressing;
import com.press.pro.enums.StatutCommande;
import com.press.pro.enums.StatutPaiement;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.press.pro.Entity.Commande;
import org.springframework.data.jpa.repository.Modifying;
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




    @Query("""
    SELECT COALESCE(SUM(c.montantPaye), 0)
    FROM Commande c
    WHERE c.pressing.id = :pressingId
      AND c.statutPaiement = 'PAYE'
      AND FUNCTION('MONTH', c.dateReception) = FUNCTION('MONTH', CURRENT_DATE)
      AND FUNCTION('YEAR', c.dateReception) = FUNCTION('YEAR', CURRENT_DATE)
""")
    Double sumCAMensuelExact(@Param("pressingId") Long pressingId);


    // Somme du reste à payer pour un pressing donné
    @Query("SELECT COALESCE(SUM(c.resteAPayer), 0.0) FROM Commande c WHERE c.pressing.id = :pressingId")
    Double sumResteAPayerByPressing(@Param("pressingId") Long pressingId);

    // Réinitialiser le montant payé aujourd'hui pour toutes les commandes
    @Modifying
    @Transactional
    @Query("UPDATE Commande c SET c.montantPayeAujourdHui = 0")
    void resetMontantPayeAujourdHui();


    // Réinitialiser le montant payé dans la semaine pour toutes les commandes
    @Modifying
    @Transactional
    @Query("UPDATE Commande c SET c.montantPayeSemaine = 0")
    void resetMontantPayeSemaine();

    // Somme des montants payés dans une semaine pour un pressing donné
    @Query(value = """
    SELECT COALESCE(SUM(montant_paye), 0)
    FROM commande
    WHERE pressing_id = :pressingId
      AND statut_paiement = 'PAYE'
      AND YEARWEEK(date_reception, 1) = YEARWEEK(CURDATE(), 1)
""", nativeQuery = true)
    BigDecimal sumCASemaineNative(@Param("pressingId") Long pressingId);


    // 🔹 CA journalier fiable pour un pressing
    @Query("SELECT COALESCE(SUM(c.montantPaye), 0) FROM Commande c " +
            "WHERE c.pressing.id = :pressingId AND DATE(c.updatedAt) = CURRENT_DATE")
    BigDecimal sumCAJournalier(@Param("pressingId") Long pressingId);





    // 🔹 CA total pour un pressing
    @Query("""
        SELECT COALESCE(SUM(c.montantPaye), 0)
        FROM Commande c
        WHERE c.pressing.id = :pressingId
          AND c.statutPaiement = 'PAYE'
    """)
    Long getChiffreAffaires(@Param("pressingId") Long pressingId);



    // 🔹 Méthode pour récupérer toutes les commandes d'un pressing avec statut PAYE
    List<Commande> findAllByPressingIdAndStatutPaiement(Long pressingId, StatutPaiement paye);

    // 🔹 Méthode pour récupérer directement le chiffre d'affaires total (somme)
    @Query("SELECT SUM(c.montantPaye) FROM Commande c WHERE c.pressing.id = :pressingId AND c.statutPaiement = :statut")
    Long getChiffreAffaires(@Param("pressingId") Long pressingId, @Param("statut") StatutPaiement statut);




    /// //// 14 - 02
    /// details commande livrees
    @Query("""
    SELECT c FROM Commande c
    WHERE c.statut = :statut
    AND c.pressing.id = :pressingId
    AND c.dateLivraison = :date
""")
    List<Commande> findLivreesParJour(
            @Param("statut") StatutCommande statut,
            @Param("pressingId") Long pressingId,
            @Param("date") LocalDate date
    );


    // 🔹 ici dashboard livraison liste
    List<Commande> findByStatutAndPressingIdAndDateLivraison(
            StatutCommande statut,
            Long pressingId,
            LocalDate dateLivraison
    );
}
