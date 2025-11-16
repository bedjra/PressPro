package com.press.pro.service.Pdf;

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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

@Service
public class CommandePdfService {

    // Dossier racine pour tous les PDF
    private static final String PDF_BASE_FOLDER = "pdfCommandes/";

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
                    Image logo = null;
                    String logoPath = pressing.getLogo().trim();

                    if (logoPath.startsWith("http://") || logoPath.startsWith("https://")) {
                        try {
                            java.net.URL url = new java.net.URL(logoPath);
                            logo = Image.getInstance(url);
                        } catch (Exception e) {
                            System.err.println("Erreur chargement logo depuis URL: " + e.getMessage());
                        }
                    } else {
                        try {
                            java.nio.file.Path path = java.nio.file.Paths.get(logoPath);
                            if (java.nio.file.Files.exists(path)) {
                                logo = Image.getInstance(path.toAbsolutePath().toString());
                            } else {
                                System.err.println("Fichier logo introuvable: " + logoPath);
                            }
                        } catch (Exception e) {
                            System.err.println("Erreur chargement logo depuis fichier: " + e.getMessage());
                        }
                    }

                    if (logo != null) {
                        logo.scaleToFit(40, 40);
                        logoCell.addElement(logo);
                    }
                } catch (Exception e) {
                    System.err.println("Erreur générale lors du chargement du logo: " + e.getMessage());
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

            // === Paiement ===
            totalTable.addCell(createCellRight("Montant déjà payé :", fontTableHeader));
            totalTable.addCell(createCellRight(String.format("%.0f F", commande.getMontantPaye()), fontTable));

            totalTable.addCell(createCellRight("Reste à payer :", fontTableHeader));
            totalTable.addCell(createCellRight(String.format("%.0f F", commande.getMontantNet() - commande.getMontantPaye()), fontSection));

            document.add(totalTable);

            document.add(Chunk.NEWLINE);
            document.add(new LineSeparator());
            document.add(Chunk.NEWLINE);

            // === Pied de page ===
            Paragraph footer = new Paragraph("Facture générée par Press-Pro", fontInfo);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(10);
            document.add(footer);

            // === Signature ===
            Paragraph signature = new Paragraph("Signature", fontInfo);
            signature.setAlignment(Element.ALIGN_RIGHT);
            signature.setSpacingBefore(10);
            document.add(signature);

            document.close();

            // === Sauvegarde PDF dans le dossier du pressing ===
            String pressingName = pressing.getNom().replaceAll("[^a-zA-Z0-9]", "_");
            String pressingFolder = PDF_BASE_FOLDER + pressingName + "/";
            Files.createDirectories(Paths.get(pressingFolder));

            String pdfFilePath = pressingFolder + "commande_" + commande.getId() + ".pdf";
            try (FileOutputStream fos = new FileOutputStream(pdfFilePath)) {
                fos.write(out.toByteArray());
            }

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
}
