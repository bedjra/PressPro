package com.press.pro.repository;


import com.press.pro.Entity.Pressing;
import org.springframework.data.jpa.repository.JpaRepository;
import com.press.pro.Entity.Client;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    // ⚡ Récupérer tous les clients d'un pressing spécifique
    List<Client> findByPressing(Pressing pressing);

    @Query("SELECT c FROM Client c WHERE LOWER(c.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR c.telephone LIKE %:keyword%")
    List<Client> searchByNomOrTelephone(@Param("keyword") String keyword);
}
