package com.press.pro.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import com.press.pro.Entity.Client;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByTelephoneOrNom(String telephone, String nom);

    @Query("SELECT c FROM Client c WHERE LOWER(c.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR c.telephone LIKE %:keyword%")
    List<Client> searchByNomOrTelephone(@Param("keyword") String keyword);
}
