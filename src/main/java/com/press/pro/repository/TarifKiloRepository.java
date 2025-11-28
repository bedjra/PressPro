package com.press.pro.repository;


import com.press.pro.Entity.TarifKilo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TarifKiloRepository extends JpaRepository<TarifKilo, Long> {

    // Récupérer tous les tarifs du pressing connecté
    List<TarifKilo> findByPressingId(Long pressingId);

    // Vérifier si un tarif existe déjà pour éviter doublons
    boolean existsByPressingIdAndTranchePoidsAndService(Long pressingId, String tranchePoids, String service);

}
