package com.press.pro.repository;



import com.press.pro.Entity.Client;
import com.press.pro.Entity.Pressing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    // 🔹 Récupérer un client unique avec son pressing pour éviter les doublons
    @Query("SELECT DISTINCT c FROM Client c LEFT JOIN FETCH c.pressing WHERE c.id = :id")
    Optional<Client> findDistinctByIdWithPressing(@Param("id") Long id);


    // 🔹 Récupérer tous les clients d’un pressing avec DISTINCT pour éviter les doublons
    @Query("SELECT DISTINCT c FROM Client c LEFT JOIN FETCH c.pressing WHERE c.pressing = :pressing")
    List<Client> findAllByPressing(@Param("pressing") Pressing pressing);


    List<Client> findByPressing(Pressing pressing);



}
