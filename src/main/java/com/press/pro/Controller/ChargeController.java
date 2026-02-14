package com.press.pro.Controller;

import com.press.pro.Dto.ChargeDTO;
import com.press.pro.Entity.Charge;
import com.press.pro.service.ChargeService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/charge")
public class ChargeController {

    private final ChargeService chargeService;

    public ChargeController(ChargeService chargeService) {
        this.chargeService = chargeService;
    }

    @GetMapping
    public List<ChargeDTO> getAll() {
        return chargeService.findAll();
    }

    @GetMapping("/{id}")
    public ChargeDTO getById(@PathVariable Long id) {
        return chargeService.findById(id);
    }

    @PostMapping
    public ChargeDTO create(@RequestBody Charge charge) {
        return chargeService.create(charge);
    }

    @PutMapping("/{id}")
    public ChargeDTO update(@PathVariable Long id, @RequestBody Charge charge) {
        return chargeService.update(id, charge);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        chargeService.delete(id);
    }

    @GetMapping("/totaux")
    public BigDecimal getTotalCharges() {
        return chargeService.getTotalCharges();
    }


    @GetMapping("/mois")
    public BigDecimal getTotalChargesMoisCourant() {
        return chargeService.getTotalChargesMoisCourant();
    }

    // 🔹 TOTAL DES CHARGES DE L’ANNÉE COURANTE
    // ex: /api/charges/total-annee
    @GetMapping("/annee")
    public BigDecimal getTotalChargesAnneeCourante() {
        return chargeService.getTotalChargesAnneeCourante();
    }


    @GetMapping("/mois/liste")
    public List<ChargeDTO> getChargesMoisCourant() {
        return chargeService.findChargesMoisCourant();
    }



}
