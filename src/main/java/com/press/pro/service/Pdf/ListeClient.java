package com.press.pro.service.Pdf;


import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.press.pro.Entity.Client;
import com.press.pro.Entity.Utilisateur;
import com.press.pro.repository.ClientRepository;
import com.press.pro.repository.UtilisateurRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Optional;

@Service
public class ListeClient {

    private final ClientRepository clientRepository;
    private final UtilisateurRepository utilisateurRepository;

    public ListeClient(ClientRepository clientRepository,
                       UtilisateurRepository utilisateurRepository) {
        this.clientRepository = clientRepository;
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

    // 📄 Générer le PDF des clients du pressing connecté
    public byte[] generatePdf() {
        Utilisateur user = getUserConnecte();

        // ✅ Récupérer tous les clients du pressing de l'utilisateur
        List<Client> clients = clientRepository.findAll() // on filtrera ensuite par pressing
                .stream()
                .filter(c -> c.getPressing() != null &&
                        c.getPressing().getId().equals(user.getPressing().getId()))
                .toList();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, out);
            document.open();

            // 🔹 TITRE
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("Liste des Clients", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // 🔹 TABLEAU
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2f, 3f, 3f, 4f, 3f});

            addHeader(table, "ID");
            addHeader(table, "Nom");
            addHeader(table, "Téléphone");
            addHeader(table, "Adresse");
            addHeader(table, "Statut");

            // 🔹 Données
            for (Client c : clients) {
                table.addCell("CLT-" + String.format("%05d", c.getId()));
                table.addCell(c.getNom());
                table.addCell(c.getTelephone());
                table.addCell(c.getAdresse());
                table.addCell(c.getStatutClient() != null ? c.getStatutClient().name() : "-");
            }

            document.add(table);
            document.close();

            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du PDF des clients : " + e.getMessage());
        }
    }

    private void addHeader(PdfPTable table, String text) {
        PdfPCell header = new PdfPCell();
        header.setBackgroundColor(BaseColor.LIGHT_GRAY);
        header.setPadding(5);
        header.setPhrase(new Phrase(text, new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD)));
        table.addCell(header);
    }
}

