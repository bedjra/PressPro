package com.press.pro.service;

import com.press.pro.Dto.ChargeDTO;
import com.press.pro.Entity.Charge;
import com.press.pro.Entity.Utilisateur;
import com.press.pro.repository.ChargeRepository;
import com.press.pro.repository.UtilisateurRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChargeService {

    private final ChargeRepository chargeRepository;
    private final UtilisateurRepository utilisateurRepository;

    public ChargeService(ChargeRepository chargeRepository,
                         UtilisateurRepository utilisateurRepository) {
        this.chargeRepository = chargeRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    // Récupère l'utilisateur connecté (comme les autres KPIs)
    private Utilisateur getUserConnecte() {
        String email = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(auth -> auth.getName())
                .orElseThrow(() -> new RuntimeException("Aucun utilisateur connecté !"));

        // Utilisation de la méthode existante
        Optional<Utilisateur> optionalUser = utilisateurRepository.findDistinctByEmailWithPressing(email.toLowerCase().trim());

        Utilisateur user = optionalUser.orElseThrow(() -> new RuntimeException("Utilisateur connecté introuvable : " + email));

        if (user.getPressing() == null)
            throw new RuntimeException("Aucun pressing associé à cet utilisateur !");

        return user;
    }

    // Mapping entity -> DTO
    private ChargeDTO toDTO(Charge charge) {
        return new ChargeDTO(
                charge.getId(),
                charge.getDescription(),
                charge.getMontant(),
                charge.getDateCharge(),
                charge.getPressing().getId(),
                charge.getPressing().getNom()
        );
    }

    public ChargeDTO create(Charge charge) {
        Utilisateur user = getUserConnecte();
        charge.setPressing(user.getPressing());
        Charge saved = chargeRepository.save(charge);
        return toDTO(saved);
    }

    public List<ChargeDTO> findAll() {
        Utilisateur user = getUserConnecte();
        // Récupération distincte des charges pour ce pressing
        List<Charge> charges = chargeRepository.findDistinctByPressingId(user.getPressing().getId());
        return charges.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ChargeDTO findById(Long id) {
        Charge charge = chargeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Charge introuvable avec l'id : " + id));
        return toDTO(charge);
    }

    public ChargeDTO update(Long id, Charge updatedCharge) {
        Utilisateur user = getUserConnecte();

        Charge charge = chargeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Charge introuvable avec l'id : " + id));

        charge.setDescription(updatedCharge.getDescription());
        charge.setMontant(updatedCharge.getMontant());
        charge.setPressing(user.getPressing());

        return toDTO(chargeRepository.save(charge));
    }

    public void delete(Long id) {
        chargeRepository.deleteById(id);
    }


    public BigDecimal getTotalCharges() {
        Utilisateur user = getUserConnecte();
        Long pressingId = user.getPressing().getId();

        return chargeRepository.sumByPressingId(pressingId);
    }



    ///  /charges annuel
    public BigDecimal getTotalChargesAnneeCourante() {
        Utilisateur user = getUserConnecte();
        Long pressingId = user.getPressing().getId();

        int annee = LocalDate.now().getYear();

        LocalDate start = LocalDate.of(annee, 1, 1);
        LocalDate end = LocalDate.of(annee, 12, 31);

        return chargeRepository
                .sumChargesBetweenDatesAndPressing(start, end, pressingId);
    }



    // 14 - 02






    public BigDecimal getTotalChargesMoisCourant() {
        return chargeRepository.getTotalMoisCourant();
    }

    public List<ChargeDTO> findChargesMoisCourant() {
        return chargeRepository.findMoisCourant()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }






    public BigDecimal getTotalChargesMensuel(int mois, int annee) {

        Utilisateur user = getUserConnecte();

        LocalDate debut = LocalDate.of(annee, mois, 1);
        LocalDate fin = debut.withDayOfMonth(debut.lengthOfMonth());

        BigDecimal charges = chargeRepository
                .sumChargesBetweenDatesAndPressing(debut, fin, user.getPressing().getId());

        return (charges != null) ? charges : BigDecimal.ZERO;
    }


}
