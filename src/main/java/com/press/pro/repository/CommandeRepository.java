package com.press.pro.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import com.press.pro.Entity.Commande;

public interface CommandeRepository extends JpaRepository<Commande, Long> {
}
