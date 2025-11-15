package com.press.pro.service.Pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.press.pro.Entity.Commande;
import com.press.pro.Entity.Utilisateur;
import com.press.pro.repository.CommandeRepository;
import com.press.pro.repository.UtilisateurRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Optional;

@Service
public class ListeCommande {

    private final CommandeRepository commandeRepository;
    private final UtilisateurRepository utilisateurRepository;

    public ListeCommande(CommandeRepository commandeRepository,
                         UtilisateurRepository utilisateurRepository) {
        this.commandeRepository = commandeRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    // 🔒 Récupération de l'utilisateur connecté
    private Utilisateur getUserConnecte() {
        String email = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(auth -> auth.getName())
                .orElseThrow(() -> new RuntimeException("Aucun utilisateur connecté !"));

        Utilisateur user = utilisateurRepository
                .findDistinctByEmailWithPressing(email.toLowerCase().trim())
                .orElseThrow(() -> new RuntimeException("Utilisateur connecté introuvable : " + email));

        if (user.getPressing() == null)
            throw new RuntimeException("Aucun pressing associé à cet utilisateur !");

        return user;
    }

    // 📄 Générer le PDF des commandes
    public byte[] generatePdf() {
        Utilisateur user = getUserConnecte();

        // ✅ Récupérer toutes les commandes du pressing
        List<Commande> commandes = commandeRepository.findAllByPressing(user.getPressing());

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, out);
            document.open();

            // 🔹 TITRE
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("Liste des Commandes", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // 🔹 TABLEAU
            PdfPTable table = new PdfPTable(6); // nombre de colonnes
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2f, 3f, 2f, 2f, 2f, 3f});

            // 🔹 En-têtes
            addHeader(table, "ID");
            addHeader(table, "Client");
            addHeader(table, "Qte");
            addHeader(table, "Montant Net");
            addHeader(table, "Montant Payé");
            addHeader(table, "Date Livraison");

            // 🔹 Données
            for (Commande c : commandes) {
                table.addCell("CMD-" + String.format("%05d", c.getId())); // ID formaté
                table.addCell(c.getClient().getNom());
                table.addCell(String.valueOf(c.getQte()));
                table.addCell(String.valueOf(c.getMontantNet()));
                table.addCell(String.valueOf(c.getMontantPaye()));
                table.addCell(c.getDateLivraison() != null ? c.getDateLivraison().toString() : "-");
            }

            document.add(table);
            document.close();

            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du PDF des commandes : " + e.getMessage());
        }
    }

    // Méthode pour créer un header de tableau
    private void addHeader(PdfPTable table, String text) {
        PdfPCell header = new PdfPCell();
        header.setBackgroundColor(BaseColor.LIGHT_GRAY);
        header.setPadding(5);
        header.setPhrase(new Phrase(text, new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD)));
        table.addCell(header);
    }
}
