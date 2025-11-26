package com.press.pro.repository;

import com.press.pro.Entity.CommandeLigne;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface CommandeLigneRepository extends JpaRepository<CommandeLigne, Long> {


}
