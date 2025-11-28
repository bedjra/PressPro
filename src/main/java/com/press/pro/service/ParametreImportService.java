package com.press.pro.service;

import com.press.pro.Entity.Parametre;
import com.press.pro.Entity.Pressing;
import com.press.pro.Entity.Utilisateur;
import com.press.pro.repository.ParametreRepository;
import com.press.pro.repository.UtilisateurRepository;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Service
public class ParametreImportService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private ParametreRepository parametreRepository;


    private Pressing getPressingUser() {
        String email = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(auth -> auth.getName())
                .orElseThrow(() -> new RuntimeException("Aucun utilisateur connecté !"));

        Utilisateur user = utilisateurRepository.findDistinctByEmailWithPressing(email.toLowerCase().trim())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé : " + email));

        if (user.getPressing() == null)
            throw new RuntimeException("Cet utilisateur n'a pas de pressing associé.");

        return user.getPressing();
    }

    public void importerParametres() {

        Pressing pressing = getPressingUser();

        List<Parametre> liste = new ArrayList<>();

        Object[][] data = {

                // --- Vêtements simples ---
                {"Chemise", "Lavage seul", 150.0},
                {"Chemise", "Lavage + Séchage", 250.0},
                {"Chemise", "L+S + Repassage", 400.0},
                {"Chemise", "Lavage Express", 700.0},

                {"Polo", "Lavage seul", 150.0},
                {"Polo", "Lavage + Séchage", 250.0},
                {"Polo", "L+S + Repassage", 400.0},
                {"Polo", "Lavage Express", 700.0},

                {"Tee-shirt", "Lavage seul", 150.0},
                {"Tee-shirt", "Lavage + Séchage", 250.0},
                {"Tee-shirt", "L+S + Repassage", 400.0},
                {"Tee-shirt", "Lavage Express", 700.0},

                {"débardeur", "Lavage seul", 75.0},
                {"débardeur", "Lavage + Séchage", 100.0},
                {"débardeur", "L+S + Repassage", 100.0},
                {"débardeur", "Lavage Express", 200.0},

                {"Pantalon", "Lavage seul", 200.0},
                {"Pantalon", "Lavage + Séchage", 300.0},
                {"Pantalon", "L+S + Repassage", 400.0},
                {"Pantalon", "Lavage Express", 700.0},

                {"Jeans", "Lavage seul", 200.0},
                {"Jeans", "Lavage + Séchage", 300.0},
                {"Jeans", "L+S + Repassage", 400.0},
                {"Jeans", "Lavage Express", 700.0},

                {"Short", "Lavage seul", 100.0},
                {"Short", "Lavage + Séchage", 150.0},
                {"Short", "L+S + Repassage", 200.0},
                {"Short", "Lavage Express", 350.0},

                {"Jupe", "Lavage seul", 100.0},
                {"Jupe", "Lavage + Séchage", 150.0},
                {"Jupe", "L+S + Repassage", 200.0},
                {"Jupe", "Lavage Express", 350.0},

                {"Robe simple", "Lavage seul", 200.0},
                {"Robe simple", "Lavage + Séchage", 300.0},
                {"Robe simple", "L+S + Repassage", 400.0},
                {"Robe simple", "Lavage Express", 700.0},

                {"Pull", "Lavage seul", 400.0},
                {"Pull", "Lavage + Séchage", 500.0},
                {"Pull", "L+S + Repassage", 600.0},
                {"Pull", "Lavage Express", 1000.0},

                {"Veste légère", "Lavage seul", 200.0},
                {"Veste légère", "Lavage + Séchage", 300.0},
                {"Veste légère", "L+S + Repassage", 500.0},
                {"Veste légère", "Lavage Express", 1000.0},

                {"Gilet", "Lavage seul", 100.0},
                {"Gilet", "Lavage + Séchage", 150.0},
                {"Gilet", "L+S + Repassage", 200.0},
                {"Gilet", "Lavage Express", 350.0},

                {"Sous-vêtements", "Lavage seul", 200.0},
                {"Sous-vêtements", "Lavage + Séchage", 300.0},
                {"Sous-vêtements", "L+S + Repassage", 350.0},
                {"Sous-vêtements", "Lavage Express", 400.0},

                {"Chaussettes", "Lavage seul", 100.0},
                {"Chaussettes", "Lavage + Séchage", 150.0},
                {"Chaussettes", "L+S + Repassage", 200.0},
                {"Chaussettes", "Lavage Express", 350.0},

                {"Pyjama", "Lavage seul", 200.0},
                {"Pyjama", "Lavage + Séchage", 300.0},
                {"Pyjama", "L+S + Repassage", 400.0},
                {"Pyjama", "Lavage Express", 700.0},

                {"Tenue de maison", "Lavage seul", 150.0},
                {"Tenue de maison", "Lavage + Séchage", 250.0},
                {"Tenue de maison", "L+S + Repassage", 400.0},
                {"Tenue de maison", "Lavage Express", 700.0},

                // --- Tenues africaines ---
                {"Grand boubou", "Lavage seul", 300.0},
                {"Grand boubou", "Lavage + Séchage", 400.0},
                {"Grand boubou", "L+S + Repassage", 600.0},
                {"Grand boubou", "Lavage Express", 1000.0},

                {"Bétékéli-Haut", "Lavage seul", 400.0},
                {"Bétékéli-Haut", "Lavage + Séchage", 600.0},
                {"Bétékéli-Haut", "L+S + Repassage", 900.0},
                {"Bétékéli-Haut", "Lavage Express", 1500.0},

                {"Bétékéli Complet", "Lavage seul", 700.0},
                {"Bétékéli Complet", "Lavage + Séchage", 1000.0},
                {"Bétékéli Complet", "L+S + Repassage", 1500.0},
                {"Bétékéli Complet", "Lavage Express", 2500.0},

                {"Grand Pagne Kénté", "Lavage seul", 500.0},
                {"Grand Pagne Kénté", "Lavage + Séchage", 700.0},
                {"Grand Pagne Kénté", "L+S + Repassage", 1000.0},
                {"Grand Pagne Kénté", "Lavage Express", 1600.0},

                {"Kénté 3 pièce et culotte", "Lavage seul", 700.0},
                {"Kénté 3 pièce et culotte", "Lavage + Séchage", 1000.0},
                {"Kénté 3 pièce et culotte", "L+S + Repassage", 1500.0},
                {"Kénté 3 pièce et culotte", "Lavage Express", 2500.0},

                {"Complet Pagne", "Lavage seul", 300.0},
                {"Complet Pagne", "Lavage + Séchage", 500.0},
                {"Complet Pagne", "L+S + Repassage", 750.0},
                {"Complet Pagne", "Lavage Express", 1000.0},

                {"Complet Basin", "Lavage seul", 300.0},
                {"Complet Basin", "Lavage + Séchage", 500.0},
                {"Complet Basin", "L+S + Repassage", 800.0},
                {"Complet Basin", "Lavage Express", 1300.0},

                {"Complet goodluck", "Lavage seul", 300.0},
                {"Complet goodluck", "Lavage + Séchage", 500.0},
                {"Complet goodluck", "L+S + Repassage", 800.0},
                {"Complet goodluck", "Lavage Express", 1300.0},

                {"Complet 3 pièce pagne", "Lavage seul", 400.0},
                {"Complet 3 pièce pagne", "Lavage + Séchage", 600.0},
                {"Complet 3 pièce pagne", "L+S + Repassage", 900.0},
                {"Complet 3 pièce pagne", "Lavage Express", 1500.0},

                {"Complet Provisoir", "Lavage seul", 300.0},
                {"Complet Provisoir", "Lavage + Séchage", 500.0},
                {"Complet Provisoir", "L+S + Repassage", 800.0},
                {"Complet Provisoir", "Lavage Express", 1300.0},

                {"Robe de soirée", "Lavage seul", 400.0},
                {"Robe de soirée", "Lavage + Séchage", 600.0},
                {"Robe de soirée", "L+S + Repassage", 800.0},
                {"Robe de soirée", "Lavage Express", 1300.0},

                {"Robe de mariage Gamme A", "Lavage seul", 4000.0},
                {"Robe de mariage Gamme A", "Lavage + Séchage", 5000.0},
                {"Robe de mariage Gamme A", "L+S + Repassage", 7000.0},
                {"Robe de mariage Gamme A", "Lavage Express", 10000.0},

                {"Robe de mariage Gamme B", "Lavage seul", 2000.0},
                {"Robe de mariage Gamme B", "Lavage + Séchage", 2500.0},
                {"Robe de mariage Gamme B", "L+S + Repassage", 3000.0},
                {"Robe de mariage Gamme B", "Lavage Express", 5000.0},

                {"Robe pagne", "Lavage seul", 300.0},
                {"Robe pagne", "Lavage + Séchage", 500.0},
                {"Robe pagne", "L+S + Repassage", 800.0},
                {"Robe pagne", "Lavage Express", 1300.0},

                {"Robe basin", "Lavage seul", 300.0},
                {"Robe basin", "Lavage + Séchage", 500.0},
                {"Robe basin", "L+S + Repassage", 800.0},
                {"Robe basin", "Lavage Express", 1300.0},

                {"Robe jeans", "Lavage seul", 300.0},
                {"Robe jeans", "Lavage + Séchage", 500.0},
                {"Robe jeans", "L+S + Repassage", 800.0},
                {"Robe jeans", "Lavage Express", 1300.0},

                {"Pagne simple", "Lavage seul", 100.0},
                {"Pagne simple", "Lavage + Séchage", 200.0},
                {"Pagne simple", "L+S + Repassage", 350.0},
                {"Pagne simple", "Lavage Express", 500.0},

                {"Costume complet", "Lavage seul", 400.0},
                {"Costume complet", "Lavage + Séchage", 600.0},
                {"Costume complet", "L+S + Repassage", 900.0},
                {"Costume complet", "Lavage Express", 2000.0},

                {"Blazer", "Lavage seul", 200.0},
                {"Blazer", "Lavage + Séchage", 400.0},
                {"Blazer", "L+S + Repassage", 700.0},
                {"Blazer", "Lavage Express", 1500.0},

                {"Manteaux", "Lavage seul", 400.0},
                {"Manteaux", "Lavage + Séchage", 600.0},
                {"Manteaux", "L+S + Repassage", 900.0},
                {"Manteaux", "Lavage Express", 2500.0},

                {"Lincueil", "Lavage seul", 400.0},
                {"Lincueil", "Lavage + Séchage", 600.0},
                {"Lincueil", "L+S + Repassage", 900.0},
                {"Lincueil", "Lavage Express", 3000.0},

                {"Voiles", "Lavage seul", 100.0},
                {"Voiles", "Lavage + Séchage", 200.0},
                {"Voiles", "L+S + Repassage", 400.0},
                {"Voiles", "Lavage Express", 700.0},

                {"Foulards", "Lavage seul", 50.0},
                {"Foulards", "Lavage + Séchage", 75.0},
                {"Foulards", "L+S + Repassage", 100.0},
                {"Foulards", "Lavage Express", 200.0},

                {"Cravates", "Lavage seul", 50.0},
                {"Cravates", "Lavage + Séchage", 75.0},
                {"Cravates", "L+S + Repassage", 100.0},
                {"Cravates", "Lavage Express", 200.0},

                {"Vêtements en soie, laine, cachemire, lin…", "Lavage seul", 700.0},
                {"Vêtements en soie, laine, cachemire, lin…", "Lavage + Séchage", 900.0},
                {"Vêtements en soie, laine, cachemire, lin…", "L+S + Repassage", 1500.0},
                {"Vêtements en soie, laine, cachemire, lin…", "Lavage Express", 2500.0},

                // --- Literie ---
                {"Drap 1 place", "Lavage seul", 500.0},
                {"Drap 1 place", "Lavage + Séchage", 700.0},
                {"Drap 1 place", "L+S + Repassage", 900.0},
                {"Drap 1 place", "Lavage Express", 1300.0},

                {"Drap 2 places", "Lavage seul", 700.0},
                {"Drap 2 places", "Lavage + Séchage", 900.0},
                {"Drap 2 places", "L+S + Repassage", 1200.0},
                {"Drap 2 places", "Lavage Express", 1700.0},

                {"Drap 3 places", "Lavage seul", 800.0},
                {"Drap 3 places", "Lavage + Séchage", 1100.0},
                {"Drap 3 places", "L+S + Repassage", 1400.0},
                {"Drap 3 places", "Lavage Express", 2000.0},

                {"Taies d’oreiller", "Lavage seul", 200.0},
                {"Taies d’oreiller", "Lavage + Séchage", 300.0},
                {"Taies d’oreiller", "L+S + Repassage", 400.0},
                {"Taies d’oreiller", "Lavage Express", 700.0},

                {"Couvre-lit, dessus de lit", "Lavage seul", 400.0},
                {"Couvre-lit, dessus de lit", "Lavage + Séchage", 600.0},
                {"Couvre-lit, dessus de lit", "L+S + Repassage", 800.0},
                {"Couvre-lit, dessus de lit", "Lavage Express", 1500.0},

                {"Couverture simple", "Lavage seul", 400.0},
                {"Couverture simple", "Lavage + Séchage", 600.0},
                {"Couverture simple", "L+S + Repassage", 800.0},
                {"Couverture simple", "Lavage Express", 1500.0},

                {"Couette petite", "Lavage seul", 800.0},
                {"Couette petite", "Lavage + Séchage", 1200.0},
                {"Couette petite", "L+S + Repassage", 1200.0},
                {"Couette petite", "Lavage Express", 2000.0},

                {"Couette grande", "Lavage seul", 1000.0},
                {"Couette grande", "Lavage + Séchage", 1500.0},
                {"Couette grande", "L+S + Repassage", 1500.0},
                {"Couette grande", "Lavage Express", 2800.0},

                // --- Salle de bain ---
                {"Serviette de bain (petite / moyenne)", "Lavage seul", 300.0},
                {"Serviette de bain (petite / moyenne)", "Lavage + Séchage", 400.0},
                {"Serviette de bain (petite / moyenne)", "L+S + Repassage", 500.0},
                {"Serviette de bain (petite / moyenne)", "Lavage Express", 800.0},

                {"Serviette de bain (grande)", "Lavage seul", 500.0},
                {"Serviette de bain (grande)", "Lavage + Séchage", 600.0},
                {"Serviette de bain (grande)", "L+S + Repassage", 700.0},
                {"Serviette de bain (grande)", "Lavage Express", 1000.0},

                {"Peignoir", "Lavage seul", 200.0},
                {"Peignoir", "Lavage + Séchage", 300.0},
                {"Peignoir", "L+S + Repassage", 400.0},
                {"Peignoir", "Lavage Express", 700.0},

                {"Gant de toilette", "Lavage seul", 100.0},
                {"Gant de toilette", "Lavage + Séchage", 150.0},
                {"Gant de toilette", "L+S + Repassage", 200.0},
                {"Gant de toilette", "Lavage Express", 300.0},

                {"Tapis de bain", "Lavage seul", 300.0},
                {"Tapis de bain", "Lavage + Séchage", 400.0},
                {"Tapis de bain", "L+S + Repassage", 400.0},
                {"Tapis de bain", "Lavage Express", 700.0},

                // --- Nappes & cuisine ---
                {"Nappes (petite / moyenne)", "Lavage seul", 600.0},
                {"Nappes (petite / moyenne)", "Lavage + Séchage", 800.0},
                {"Nappes (petite / moyenne)", "L+S + Repassage", 900.0},
                {"Nappes (petite / moyenne)", "Lavage Express", 1500.0},

                {"Nappes (grande)", "Lavage seul", 800.0},
                {"Nappes (grande)", "Lavage + Séchage", 1000.0},
                {"Nappes (grande)", "L+S + Repassage", 1200.0},
                {"Nappes (grande)", "Lavage Express", 1700.0},

                {"Serviettes de table", "Lavage seul", 300.0},
                {"Serviettes de table", "Lavage + Séchage", 400.0},
                {"Serviettes de table", "L+S + Repassage", 400.0},
                {"Serviettes de table", "Lavage Express", 700.0},

                {"Torchon, essuie-mains", "Lavage seul", 100.0},
                {"Torchon, essuie-mains", "Lavage + Séchage", 150.0},
                {"Torchon, essuie-mains", "L+S + Repassage", 200.0},
                {"Torchon, essuie-mains", "Lavage Express", 300.0},

                {"Tissu de buffets / housses de table", "Lavage seul", 100.0},
                {"Tissu de buffets / housses de table", "Lavage + Séchage", 150.0},
                {"Tissu de buffets / housses de table", "L+S + Repassage", 200.0},
                {"Tissu de buffets / housses de table", "Lavage Express", 300.0},

                {"Manique, gants de cuisine", "Lavage seul", 100.0},
                {"Manique, gants de cuisine", "Lavage + Séchage", 150.0},
                {"Manique, gants de cuisine", "L+S + Repassage", 200.0},
                {"Manique, gants de cuisine", "Lavage Express", 300.0},

                // --- Bébé ---
                {"Body bébé, gigoteuse", "Lavage seul", 600.0},
                {"Body bébé, gigoteuse", "Lavage Express", 1000.0},

                {"Draps bébé, petites couvertures", "Lavage seul", 400.0},
                {"Draps bébé, petites couvertures", "Lavage Express", 700.0},

                {"Doudou, peluche lavable", "Lavage seul", 300.0},
                {"Doudou, peluche lavable", "Lavage Express", 600.0},

                {"Bavoirs", "Lavage seul", 150.0},
                {"Bavoirs", "Lavage + Séchage", 200.0},
                {"Bavoirs", "Lavage Express", 400.0},

                // --- Divers ---
                {"Sac de voyage en tissu", "Lavage seul", 300.0},
                {"Sac de voyage en tissu", "Lavage + Séchage", 500.0},
                {"Sac de voyage en tissu", "Lavage Express", 1000.0},

                {"Sac à main en tissu", "Lavage seul", 300.0},
                {"Sac à main en tissu", "Lavage + Séchage", 500.0},
                {"Sac à main en tissu", "Lavage Express", 1000.0}
        };


        for (Object[] ligne : data) {
            liste.add(new Parametre(
                    (String) ligne[0],
                    (String) ligne[1],
                    (Double) ligne[2],
                    pressing
            ));
        }

        parametreRepository.saveAll(liste);
    }




}
