package com.press.pro.Controller;

import com.press.pro.Entity.Charge;
import com.press.pro.service.ChargeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/charge")
public class ChargeController {

    private final ChargeService chargeService;

    public ChargeController(ChargeService chargeService) {
        this.chargeService = chargeService;
    }

    @PostMapping
    public Charge createCharge(@RequestBody Charge charge) {
        return chargeService.create(charge);
    }

    @GetMapping
    public List<Charge> getAllCharges() {
        return chargeService.findAll();
    }

    @GetMapping("/{id}")
    public Charge getChargeById(@PathVariable Long id) {
        return chargeService.findById(id)
                .orElseThrow(() -> new RuntimeException("Charge introuvable avec l'id : " + id));
    }

    @PutMapping("/{id}")
    public Charge updateCharge(@PathVariable Long id, @RequestBody Charge charge) {
        return chargeService.update(id, charge);
    }

    @DeleteMapping("/{id}")
    public String deleteCharge(@PathVariable Long id) {
        chargeService.delete(id);
        return "Charge supprimée avec succès";
    }
}
