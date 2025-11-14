package com.press.pro.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.press.pro.Entity.Client;
import com.press.pro.Entity.Commande;
import com.press.pro.Entity.Parametre;
import com.press.pro.Entity.Pressing;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

@Service
public class CommandePdfService {

    private static final String DOSSIER_COMMANDES = "CommandesPdf";

    public byte[] genererCommandePdf(Commande commande) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A6, 25, 25, 25, 25);

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // === Styles ===
            Font fontTitre = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font fontSection = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font fontInfo = FontFactory.getFont(FontFactory.HELVETICA, 8);
            Font fontTableHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
            Font fontTable = FontFactory.getFont(FontFactory.HELVETICA, 8);

            // === En-tête Pressing ===
            Pressing pressing = commande.getPressing();
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{1f, 2.5f});

            // Logo
            PdfPCell logoCell = new PdfPCell();
            logoCell.setBorder(Rectangle.NO_BORDER);
            logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

            if (pressing.getLogo() != null && !pressing.getLogo().isBlank()) {
                try {
                    Path logoPath = Paths.get(pressing.getLogo());
                    if (Files.exists(logoPath)) {
                        Image logo = Image.getInstance(logoPath.toAbsolutePath().toString());
                        logo.scaleToFit(40, 40);
                        logoCell.addElement(logo);
                    } else {
                        System.out.println("Logo introuvable : " + pressing.getLogo());
                    }
                } catch (Exception e) {
                    System.out.println("Erreur lors de l'ajout du logo : " + e.getMessage());
                    // On continue sans logo
                }
            }
            headerTable.addCell(logoCell);


            // Infos Pressing
            PdfPCell infoCell = new PdfPCell();
            infoCell.setBorder(Rectangle.NO_BORDER);
            infoCell.addElement(new Paragraph(pressing.getNom(), fontTitre));
            infoCell.addElement(new Paragraph(pressing.getAdresse(), fontInfo));
            infoCell.addElement(new Paragraph("Tél: " + pressing.getTelephone(), fontInfo));
            infoCell.addElement(new Paragraph("Email: " + pressing.getEmail(), fontInfo));
            headerTable.addCell(infoCell);

            document.add(headerTable);
            document.add(new LineSeparator());
            document.add(Chunk.NEWLINE);

            // === Infos Commande ===
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            Paragraph pCommande = new Paragraph("Détails Commande", fontSection);
            pCommande.setSpacingAfter(5);
            document.add(pCommande);

            PdfPTable commandeTable = new PdfPTable(2);
            commandeTable.setWidthPercentage(100);
            commandeTable.setSpacingAfter(5);

            commandeTable.addCell(createCellLeft("Réception:", fontInfo));
            commandeTable.addCell(createCellLeft(commande.getDateReception().format(formatter), fontInfo));

            commandeTable.addCell(createCellLeft("Livraison:", fontInfo));
            commandeTable.addCell(createCellLeft(commande.getDateLivraison().format(formatter), fontInfo));

            // ✅ Ajout du montant payé et du statut de paiement
            commandeTable.addCell(createCellLeft("Montant payé:", fontInfo));
            commandeTable.addCell(createCellLeft(String.format("%.0f F", commande.getMontantPaye()), fontInfo));

            commandeTable.addCell(createCellLeft("Statut paiement:", fontInfo));
            commandeTable.addCell(createCellLeft(String.valueOf(commande.getStatutPaiement()), fontInfo));

            document.add(commandeTable);

            // === Infos Client ===
            Paragraph pClient = new Paragraph("Client", fontSection);
            pClient.setSpacingAfter(5);
            document.add(pClient);

            Client client = commande.getClient();
            PdfPTable clientTable = new PdfPTable(2);
            clientTable.setWidthPercentage(100);
            clientTable.setSpacingAfter(5);

            clientTable.addCell(createCellLeft("Nom:", fontInfo));
            clientTable.addCell(createCellLeft(client.getNom(), fontInfo));

            clientTable.addCell(createCellLeft("Téléphone:", fontInfo));
            clientTable.addCell(createCellLeft(client.getTelephone(), fontInfo));

            document.add(clientTable);

            // === Tableau Commande ===
            Paragraph pTable = new Paragraph("Articles", fontSection);
            pTable.setSpacingAfter(5);
            document.add(pTable);

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2.5f, 1f, 1f, 1.2f});
            table.setSpacingAfter(5);

            Stream.of("Article / Service", "Qté", "P.U", "Montant")
                    .forEach(title -> {
                        PdfPCell header = new PdfPCell(new Phrase(title, fontTableHeader));
                        header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                        header.setHorizontalAlignment(Element.ALIGN_CENTER);
                        header.setPadding(4f);
                        table.addCell(header);
                    });

            Parametre param = commande.getParametre();
            String description = param != null ? (param.getArticle() + " / " + param.getService()) : "-";
            double prixUnitaire = param != null ? param.getPrix() : 0;

            table.addCell(createCellLeft(description, fontTable));
            table.addCell(createCellCenter(String.valueOf(commande.getQte()), fontTable));
            table.addCell(createCellRight(String.format("%.0f F", prixUnitaire), fontTable));
            table.addCell(createCellRight(String.format("%.0f F", commande.getMontantBrut()), fontTable));

            document.add(table);

            // === Totaux ===
            PdfPTable totalTable = new PdfPTable(2);
            totalTable.setWidthPercentage(80);
            totalTable.setHorizontalAlignment(Element.ALIGN_RIGHT);

            totalTable.addCell(createCellRight("Brut :", fontTableHeader));
            totalTable.addCell(createCellRight(String.format("%.0f F", commande.getMontantBrut()), fontTable));

            totalTable.addCell(createCellRight("Remise :", fontTableHeader));
            totalTable.addCell(createCellRight(String.format("%.0f F", commande.getRemise()), fontTable));

            totalTable.addCell(createCellRight("Net à payer :", fontSection));
            totalTable.addCell(createCellRight(String.format("%.0f F", commande.getMontantNet()), fontSection));

            document.add(totalTable);

            document.add(Chunk.NEWLINE);
            document.add(new LineSeparator());
            document.add(Chunk.NEWLINE);

            // === Signature ===
            Paragraph signature = new Paragraph("Signature", fontInfo);
            signature.setAlignment(Element.ALIGN_RIGHT);
            signature.setSpacingBefore(10);
            document.add(signature);

            document.close();

            sauvegarderPdf(out.toByteArray(), commande.getId());

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la génération du PDF : " + e.getMessage());
        }

        return out.toByteArray();
    }

    // === Méthodes utilitaires ===
    private PdfPCell createCellLeft(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPadding(3f);
        return cell;
    }

    private PdfPCell createCellCenter(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3f);
        return cell;
    }

    private PdfPCell createCellRight(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setPadding(3f);
        return cell;
    }

    private void sauvegarderPdf(byte[] pdfBytes, Long idCommande) throws IOException {
        Path dossier = Paths.get(DOSSIER_COMMANDES);
        if (!Files.exists(dossier)) Files.createDirectories(dossier);

        String filename = "Commande_" + idCommande + ".pdf";
        Path cheminComplet = dossier.resolve(filename);
        try (FileOutputStream fos = new FileOutputStream(cheminComplet.toFile())) {
            fos.write(pdfBytes);
        }
    }
}
