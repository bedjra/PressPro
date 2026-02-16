package com.press.pro.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ResultatService {

    private final CommandeService commandeService;
    private final ChargeService chargeService;

    public ResultatService(CommandeService commandeService, ChargeService chargeService) {
        this.commandeService = commandeService;
        this.chargeService = chargeService;
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


}
