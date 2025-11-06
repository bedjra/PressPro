package com.press.pro.service;

import com.press.pro.Dto.ParametreDto;
import com.press.pro.Entity.Parametre;
import com.press.pro.repository.ParametreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ParametreService {

    @Autowired
    private ParametreRepository parametreRepository;

    // 🔹 Convertir Entity -> Dto
    private ParametreDto mapToDto(Parametre parametre) {
        return new ParametreDto(
                parametre.getId(),
                parametre.getArticle(),
                parametre.getService(),
                parametre.getPrix()
        );
    }

    // 🔹 Convertir Dto -> Entity
    private Parametre mapToEntity(ParametreDto Dto) {
        Parametre parametre = new Parametre();
        parametre.setId(Dto.getId());
        parametre.setArticle(Dto.getArticle());
        parametre.setService(Dto.getService());
        parametre.setPrix(Dto.getPrix());
        return parametre;
    }

    // ✅ CREATE
    public ParametreDto createParametre(ParametreDto Dto) {
        Parametre parametre = mapToEntity(Dto);
        return mapToDto(parametreRepository.save(parametre));
    }

    // ✅ READ ALL
    public List<ParametreDto> getAllParametres() {
        return parametreRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // ✅ READ BY ID
    public Optional<ParametreDto> getParametreById(Long id) {
        return parametreRepository.findById(id)
                .map(this::mapToDto);
    }

    // ✅ UPDATE
    public Optional<ParametreDto> updateParametre(Long id, ParametreDto Dto) {
        return parametreRepository.findById(id).map(parametre -> {
            parametre.setArticle(Dto.getArticle());
            parametre.setService(Dto.getService());
            parametre.setPrix(Dto.getPrix());
            return mapToDto(parametreRepository.save(parametre));
        });
    }

    // ✅ DELETE
    public boolean deleteParametre(Long id) {
        if (parametreRepository.existsById(id)) {
            parametreRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
