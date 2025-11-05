package com.press.pro.service;


import com.press.pro.Entity.Pressing;
import com.press.pro.repository.PressingRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PressingService {

    private final PressingRepository pressingRepository;

    public PressingService(PressingRepository pressingRepository) {
        this.pressingRepository = pressingRepository;
    }

    public List<Pressing> getAllPressings() {
        return pressingRepository.findAll();
    }

    public Optional<Pressing> getPressingById(Long id) {
        return pressingRepository.findById(id);
    }

    public Pressing createPressing(Pressing pressing) {
        return pressingRepository.save(pressing);
    }

    public Pressing updatePressing(Long id, Pressing pressingDetails) {
        Pressing pressing = pressingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pressing non trouvé avec l'id : " + id));

        pressing.setNom(pressingDetails.getNom());
        pressing.setEmail(pressingDetails.getEmail());
        pressing.setTelephone(pressingDetails.getTelephone());
        pressing.setAdresse(pressingDetails.getAdresse());

        return pressingRepository.save(pressing);
    }

    public void deletePressing(Long id) {
        pressingRepository.deleteById(id);
    }
}
