package com.press.pro.repository;


import com.press.pro.Entity.Pressing;
import com.press.pro.enums.StatutCommande;
import org.springframework.data.jpa.repository.JpaRepository;
import com.press.pro.Entity.Commande;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CommandeRepository extends JpaRepository<Commande, Long> {

    List<Commande> findAllByPressing(Pressing pressing);

    @Query("SELECT c FROM Commande c JOIN FETCH c.pressing WHERE c.id = :id")
    Optional<Commande> findDistinctByIdWithPressing(Long id);

    @Query("SELECT c.dateReception, COUNT(c) FROM Commande c GROUP BY c.dateReception ORDER BY c.dateReception DESC")
    List<Object[]> countCommandesByDay();

    @Query("SELECT c.dateReception, COUNT(c) FROM Commande c WHERE c.statut = :statut GROUP BY c.dateReception ORDER BY c.dateReception DESC")
    List<Object[]> countCommandesByStatutAndDay(StatutCommande statut);
}
