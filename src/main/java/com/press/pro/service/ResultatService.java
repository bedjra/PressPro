package com.press.pro.service;

import com.press.pro.Entity.Utilisateur;
import com.press.pro.repository.ChargeRepository;
import com.press.pro.repository.CommandeRepository;
import com.press.pro.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class ResultatService {

    private final CommandeService commandeService;
    private final ChargeService chargeService;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private ChargeRepository chargeRepository;

    @Autowired
    private CommandeRepository commandeRepository;


    public ResultatService(CommandeService commandeService, ChargeService chargeService) {
        this.commandeService = commandeService;
        this.chargeService = chargeService;
    }


    // Récupérer l'utilisateur connecté
    private Utilisateur getUserConnecte() {
        String email = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(auth -> auth.getName())
                .orElseThrow(() -> new RuntimeException("Aucun utilisateur connecté !"));

        return utilisateurRepository.findDistinctByEmailWithPressing(email.toLowerCase().trim())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : " + email));
    }

    public BigDecimal getResultatNet() {
        BigDecimal totalCa = commandeService.getChiffreAffairesTotal();

        BigDecimal totalCharges = chargeService.getTotalCharges();

        return totalCa.subtract(totalCharges); // résultat = CA - Charges
    }


    public BigDecimal getResultatNetMensuel(int mois, int annee) {

        Double ca = commandeService.getCAMensuel(mois, annee);

        BigDecimal charges = chargeService.getTotalChargesMensuel(mois, annee);

        return BigDecimal.valueOf(ca).subtract(charges);
    }


    /**
     * Retourne le résultat net par mois pour un pressing donné
     * Format : { année -> { mois -> net } }
     */
    public Map<Integer, Map<Integer, BigDecimal>> getResultatNetParMois(Long pressingId) {
        Map<Integer, Map<Integer, BigDecimal>> netParMois = new HashMap<>();

        // 1️⃣ Récupérer le CA par mois
        List<Object[]> caParMois = commandeRepository.sumCAParMois(pressingId);

        for (Object[] row : caParMois) {
            Integer annee = ((Number) row[0]).intValue();
            Integer mois = ((Number) row[1]).intValue();
            BigDecimal ca = row[2] != null ? new BigDecimal(row[2].toString()) : BigDecimal.ZERO;

            // 2️⃣ Récupérer les charges pour le même mois
            java.time.LocalDate debut = java.time.LocalDate.of(annee, mois, 1);
            java.time.LocalDate fin = debut.withDayOfMonth(debut.lengthOfMonth());

            BigDecimal charges = chargeRepository.sumChargesBetweenDatesAndPressing(debut, fin, pressingId);
            if (charges == null) charges = BigDecimal.ZERO;

            // 3️⃣ Calcul du net = CA - Charges
            BigDecimal net = ca.subtract(charges);

            // 4️⃣ Stocker dans la map
            netParMois
                    .computeIfAbsent(annee, y -> new HashMap<>())
                    .put(mois, net);
        }

        return netParMois;
    }

}
