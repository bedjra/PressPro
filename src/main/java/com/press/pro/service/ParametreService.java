package com.press.pro.service;

import com.press.pro.Dto.ParametreDto;
import com.press.pro.Entity.Parametre;
import com.press.pro.Entity.Utilisateur;
import com.press.pro.repository.ParametreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ParametreService {

    @Autowired
    private ParametreRepository parametreRepository;

    // 🔹 Convertir Parametre → ParametreDto
    private ParametreDto toDto(Parametre param) {
        ParametreDto dto = new ParametreDto();
        dto.setId(param.getId());
        dto.setArticle(param.getArticle());
        dto.setService(param.getService());
        dto.setPrix(param.getPrix());
        return dto;
    }

    // 🔹 Convertir ParametreDto → Parametre
    private Parametre toEntity(ParametreDto dto) {
        Parametre param = new Parametre();
        param.setArticle(dto.getArticle());
        param.setService(dto.getService());
        param.setPrix(dto.getPrix());
        return param;
    }

    // ✅ Créer un paramètre
    public ParametreDto createParametre(ParametreDto dto, Utilisateur utilisateurConnecte) {
        Parametre param = toEntity(dto);
        param.setPressing(utilisateurConnecte.getPressing());

        Parametre saved = parametreRepository.save(param);
        return toDto(saved);
    }



    // ✅ Récupérer un paramètre par ID
    public ParametreDto getParametreById(Long id, Utilisateur utilisateurConnecte) {
        Parametre param = parametreRepository.findDistinctByIdWithPressing(id)
                .orElseThrow(() -> new RuntimeException("Paramètre non trouvé"));

        if (!param.getPressing().getId().equals(utilisateurConnecte.getPressing().getId())) {
            throw new RuntimeException("Accès refusé : paramètre d'un autre pressing");
        }

        return toDto(param);
    }

    // ✅ Mettre à jour un paramètre
    public ParametreDto updateParametre(Long id, ParametreDto dto, Utilisateur utilisateurConnecte) {
        Parametre param = parametreRepository.findDistinctByIdWithPressing(id)
                .orElseThrow(() -> new RuntimeException("Paramètre non trouvé"));

        if (!param.getPressing().getId().equals(utilisateurConnecte.getPressing().getId())) {
            throw new RuntimeException("Accès refusé : paramètre d'un autre pressing");
        }

        param.setArticle(dto.getArticle());
        param.setService(dto.getService());
        param.setPrix(dto.getPrix());

        Parametre saved = parametreRepository.save(param);
        return toDto(saved);
    }

    // ✅ Supprimer un paramètre
    public void deleteParametre(Long id, Utilisateur utilisateurConnecte) {
        Parametre param = parametreRepository.findDistinctByIdWithPressing(id)
                .orElseThrow(() -> new RuntimeException("Paramètre non trouvé"));

        if (!param.getPressing().getId().equals(utilisateurConnecte.getPressing().getId())) {
            throw new RuntimeException("Accès refusé : paramètre d'un autre pressing");
        }

        parametreRepository.delete(param);
    }

    public List<ParametreDto> getAllParametres(Utilisateur utilisateurConnecte) {
        Long pressingId = utilisateurConnecte.getPressing().getId();

        return parametreRepository.findAllByPressingId(pressingId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

}
