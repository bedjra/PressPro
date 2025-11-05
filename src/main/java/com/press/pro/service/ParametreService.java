//package com.press.pro.service;
//
//
//import com.press.pro.Entity.Parametre;
//import com.press.pro.repository.ParametreRepository;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Optional;
//
//@Service
//public class ParametreService {
//
//    private final ParametreRepository parametreRepository;
//
//    public ParametreService(ParametreRepository parametreRepository) {
//        this.parametreRepository = parametreRepository;
//    }
//
//    public List<Parametre> getAllParametres() {
//        return parametreRepository.findAll();
//    }
//
//    public Optional<Parametre> getParametreById(Long id) {
//        return parametreRepository.findById(id);
//    }
//
//    public Parametre addParametre(Parametre parametre) {
//        return parametreRepository.save(parametre);
//    }
//
//    public Parametre updateParametre(Long id, Parametre parametre) {
//        return parametreRepository.findById(id)
//                .map(existing -> {
//                    existing.setArticle(parametre.getArticle());
//                    existing.setService(parametre.getService());
//                    existing.setPrix(parametre.getPrix());
//                    return parametreRepository.save(existing);
//                })
//                .orElseThrow(() -> new RuntimeException("Paramètre non trouvé"));
//    }
//
//    public void deleteParametre(Long id) {
//        parametreRepository.deleteById(id);
//    }
//}
