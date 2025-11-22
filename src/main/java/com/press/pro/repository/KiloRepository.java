package com.press.pro.repository;

import com.press.pro.Entity.Kilo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KiloRepository extends JpaRepository<Kilo, Long> {
}
