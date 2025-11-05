package com.press.pro.repository;

import com.press.pro.Entity.Pressing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PressingRepository extends JpaRepository<Pressing, Long> {
}