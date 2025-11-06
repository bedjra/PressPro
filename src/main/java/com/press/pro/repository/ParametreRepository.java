package com.press.pro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.press.pro.Entity.Parametre;

import java.util.Optional;

public interface ParametreRepository extends JpaRepository<Parametre, Long> {
//    Optional<Parametre> findByArticleAndService(String article, String service);

}
