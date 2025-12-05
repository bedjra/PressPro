package com.press.pro.service;


import com.press.pro.Dto.TarifKiloDto;
import com.press.pro.Entity.Pressing;
import com.press.pro.Entity.TarifKilo;
import com.press.pro.Entity.Utilisateur;
import com.press.pro.repository.TarifKiloRepository;
import com.press.pro.repository.UtilisateurRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class TarifKiloService {

    @Autowired
    private TarifKiloRepository tarifKiloRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    // ----------------------------
    // 🔐 Récupération user connecté
    // ----------------------------
    private Utilisateur getUserConnecte() {
        String email = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(auth -> auth.getName())
                .orElseThrow(() -> new RuntimeException("Aucun utilisateur connecté !"));

        Optional<Utilisateur> optionalUser =
                utilisateurRepository.findDistinctByEmailWithPressing(email.toLowerCase().trim());

        Utilisateur user = optionalUser.orElseThrow(() ->
                new RuntimeException("Utilisateur connecté introuvable : " + email));

        if (user.getPressing() == null)
            throw new RuntimeException("Aucun pressing associé à cet utilisateur !");

        return user;
    }

    // ----------------------------------------
    // ✅ Ajouter un tarif au kilo (sans doublon)
    // ----------------------------------------
    public TarifKiloDto ajouterTarif(TarifKiloDto dto) {

        Utilisateur user = getUserConnecte();
        Pressing pressing = user.getPressing();

        // Vérification doublon
        boolean exist = tarifKiloRepository.existsByPressingIdAndTranchePoidsAndService(
                pressing.getId(),
                dto.getTranchePoids(),
                dto.getService()
        );

        if (exist) {
            throw new RuntimeException("Un tarif pour cette tranche et ce service existe déjà !");
        }

        TarifKilo tarif = new TarifKilo();
        tarif.setTranchePoids(dto.getTranchePoids());
        tarif.setService(dto.getService());
        tarif.setPrix(dto.getPrix());
        tarif.setPressing(pressing);

        tarif = tarifKiloRepository.save(tarif);

        dto.setId(tarif.getId());
        return dto;
    }

    // ----------------------------------------
    // ✅ Liste des tarifs du pressing connecté
    // ----------------------------------------
    public List<TarifKiloDto> listeTarifs() {
        Utilisateur user = getUserConnecte();

        return tarifKiloRepository.findByPressingId(user.getPressing().getId())
                .stream()
                .map(t -> {
                    TarifKiloDto dto = new TarifKiloDto();
                    dto.setId(t.getId());
                    dto.setTranchePoids(t.getTranchePoids());
                    dto.setService(t.getService());
                    dto.setPrix(t.getPrix());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // ----------------------------------------
    // ❌ Supprimer un tarif
    // ----------------------------------------
    public void supprimerTarif(Long id) {
        Utilisateur user = getUserConnecte();
        Long pressingId = user.getPressing().getId();

        TarifKilo tarif = tarifKiloRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarif introuvable : " + id));

        if (!tarif.getPressing().getId().equals(pressingId)) {
            throw new RuntimeException("Accès refusé : ce tarif n’appartient pas à votre pressing !");
        }

        tarifKiloRepository.delete(tarif);
    }

    // ----------------------------------------
// ✅ Récupérer un tarif par son ID (sécurisé)
// ----------------------------------------
    public TarifKiloDto getById(Long id) {
        Utilisateur user = getUserConnecte();
        Long pressingId = user.getPressing().getId();

        TarifKilo tarif = tarifKiloRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarif introuvable : " + id));

        // Sécurité : vérifier que le tarif appartient au pressing du user
        if (!tarif.getPressing().getId().equals(pressingId)) {
            throw new RuntimeException("Accès refusé : ce tarif n’appartient pas à votre pressing !");
        }

        // Conversion -> DTO
        TarifKiloDto dto = new TarifKiloDto();
        dto.setId(tarif.getId());
        dto.setTranchePoids(tarif.getTranchePoids());
        dto.setService(tarif.getService());
        dto.setPrix(tarif.getPrix());

        return dto;
    }



    public void importerTarifsParDefaut() {

        Utilisateur user = getUserConnecte();
        Pressing pressing = user.getPressing();

        // Liste des tranches avec les tarifs associés
        Object[][] DATA = {
                {"1-4Kg", 700, 1100, 1600, 2400},
                {"5-9Kg", 600, 1000, 1400, 2100},
                {"10-20Kg", 500, 800, 1200, 1900},
                {"Sup à 20Kg", 450, 700, 1000, 1700}
        };

        String[] SERVICES = {
                "Lavage seul",
                "Lavage + Séchage",
                "L+S + Repassage",
                "Lavage Express"
        };

        for (Object[] row : DATA) {

            String tranche = (String) row[0];

            for (int i = 0; i < SERVICES.length; i++) {

                Integer prix = (Integer) row[i + 1];
                String service = SERVICES[i];

                // Vérification doublon
                boolean exist = tarifKiloRepository
                        .existsByPressingIdAndTranchePoidsAndService(
                                pressing.getId(),
                                tranche,
                                service
                        );

                if (!exist) {
                    TarifKilo tarif = new TarifKilo();
                    tarif.setTranchePoids(tranche);
                    tarif.setService(service);
                    tarif.setPrix(prix);
                    tarif.setPressing(pressing);

                    tarifKiloRepository.save(tarif);
                }
            }
        }
    }

}
