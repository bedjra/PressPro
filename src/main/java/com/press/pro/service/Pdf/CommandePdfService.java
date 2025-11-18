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
import java.util.Locale;
import java.util.stream.Stream;

@Service
public class CommandePdfService {

    private static final String PDF_BASE_FOLDER = "pdfCommandes/";
    private static final String LOGO_FOLDER = "uploads/";

    public byte[] genererCommandePdf(Commande commande) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // 🔥 FORMAT CHANGÉ : A4 → A6 (ticket)
        Document document = new Document(PageSize.A6, 10, 10, 10, 10);

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Styles
            Font fontTitre = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font fontSousTitre = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font fontInfo = FontFactory.getFont(FontFactory.HELVETICA, 7);
            Font fontTableHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7);
            Font fontTable = FontFactory.getFont(FontFactory.HELVETICA, 7);
            Font fontMontant = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);

            Pressing pressing = commande.getPressing();

            // ENTETE
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{1f, 3f});
            headerTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            PdfPCell logoCell = new PdfPCell();
            logoCell.setBorder(Rectangle.NO_BORDER);
            logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

            try {
                if (pressing.getLogo() != null && pressing.getLogo().length > 0) {
                    Image logo = Image.getInstance(pressing.getLogo());
                    logo.scaleToFit(40, 40); // réduit pour A6
                    logoCell.addElement(logo);
                }
            } catch (Exception e) {
                System.out.println("Erreur chargement logo : " + e.getMessage());
            }

            headerTable.addCell(logoCell);

            PdfPCell infoCell = new PdfPCell();
            infoCell.setBorder(Rectangle.NO_BORDER);
            infoCell.setPaddingLeft(5f);

            Paragraph nom = new Paragraph(pressing.getNom(), fontTitre);
            infoCell.addElement(nom);
            infoCell.addElement(new Paragraph(pressing.getAdresse(), fontInfo));
            infoCell.addElement(new Paragraph("Tél: " + pressing.getTelephone(), fontInfo));
            if (pressing.getEmail() != null)
                infoCell.addElement(new Paragraph(pressing.getEmail(), fontInfo));

            headerTable.addCell(infoCell);
            document.add(headerTable);

            document.add(Chunk.NEWLINE);
            document.add(new LineSeparator());
            document.add(Chunk.NEWLINE);

            Paragraph factureTitle = new Paragraph(
                    "Reçu N° " + formatNumeroFacture(commande.getId()),
                    fontSousTitre
            );
            factureTitle.setAlignment(Element.ALIGN_CENTER);
            factureTitle.setSpacingAfter(5);
            document.add(factureTitle);

            // INFO CLIENT
            PdfPTable topInfo = new PdfPTable(2);
            topInfo.setWidthPercentage(100);
            topInfo.setWidths(new float[]{2f, 1f});
            topInfo.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            Client client = commande.getClient();

            PdfPTable left = new PdfPTable(1);
            left.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            left.addCell(createCellNoBorder("Client : " +
                    (client != null ? client.getNom() : "CLIENT DIVERS"), fontTable));

            left.addCell(createCellNoBorder("Adresse : " +
                    (client != null && client.getAdresse() != null ? client.getAdresse() : "-"), fontInfo));

            topInfo.addCell(left);

            PdfPTable right = new PdfPTable(1);
            right.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String dateFact = commande.getDateReception() != null ?
                    commande.getDateReception().format(formatter) : "";

            right.addCell(createCellNoBorder("Date : " + dateFact, fontInfo));
            right.addCell(createCellNoBorder("", fontInfo));

            topInfo.addCell(right);
            document.add(topInfo);
            document.add(Chunk.NEWLINE);

            // TABLEAU
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{0.7f, 3f, 1.1f, 1f, 1.1f, 1.3f});

            Stream.of("Qté", "Désignation", "P.V.U", "Remise", "Net", "Montant")
                    .forEach(h -> {
                        PdfPCell hd = new PdfPCell(new Phrase(h, fontTableHeader));
                        hd.setHorizontalAlignment(Element.ALIGN_CENTER);
                        hd.setPadding(3f);
                        hd.setBackgroundColor(BaseColor.LIGHT_GRAY);
                        table.addCell(hd);
                    });

            Parametre param = commande.getParametre();
            String description = param != null ? param.getArticle() : "-";

            double prixUnitaire = param != null ? param.getPrix() : 0.0;
            int qte = commande.getQte();
            double remiseTotale = commande.getRemise();
            double montantHT = prixUnitaire * qte;

            table.addCell(createCellCenter(String.valueOf(qte), fontTable));
            table.addCell(createCellLeft(description, fontTable));
            table.addCell(createCellRight(String.format("%.0f F", prixUnitaire), fontTable));
            table.addCell(createCellRight(String.format("%.0f F", remiseTotale), fontTable));
            table.addCell(createCellRight(String.format("%.0f F", prixUnitaire), fontTable));
            table.addCell(createCellRight(String.format("%.0f F", montantHT), fontTable));

            document.add(table);

            // TOTAUX
            PdfPTable outer = new PdfPTable(2);
            outer.setWidthPercentage(100);
            outer.setWidths(new float[]{1.3f, 1f});
            outer.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            PdfPCell empty = new PdfPCell(new Phrase(""));
            empty.setBorder(Rectangle.NO_BORDER);
            outer.addCell(empty);

            PdfPTable totaux = new PdfPTable(2);
            totaux.setWidthPercentage(100);
            totaux.setWidths(new float[]{2f, 1f});

            double netCommercial = montantHT - remiseTotale;
            double montantTTC = netCommercial;

            double montantPaye = commande.getMontantPaye();
            double resteAPayer = montantTTC - montantPaye;

            // ON GARDE SEULEMENT LES LIGNES DEMANDÉES
            totaux.addCell(createCellRight("Remise", fontTableHeader));
            totaux.addCell(createCellRight(String.format("%.0f F", remiseTotale), fontTable));

            totaux.addCell(createCellRight("Net Commercial", fontTableHeader));
            totaux.addCell(createCellRight(String.format("%.0f F", netCommercial), fontTable));

            PdfPCell ttcLabel = new PdfPCell(new Phrase("Montant TTC", fontTableHeader));
            ttcLabel.setHorizontalAlignment(Element.ALIGN_LEFT);
            ttcLabel.setPadding(4f);
            ttcLabel.setBackgroundColor(BaseColor.LIGHT_GRAY);

            PdfPCell ttcValue = new PdfPCell(new Phrase(String.format("%.0f F", montantTTC), fontMontant));
            ttcValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
            ttcValue.setPadding(4f);
            ttcValue.setBackgroundColor(BaseColor.LIGHT_GRAY);

            totaux.addCell(ttcLabel);
            totaux.addCell(ttcValue);

            totaux.addCell(createCellRight("Montant Payé", fontTableHeader));
            totaux.addCell(createCellRight(String.format("%.0f F", montantPaye), fontTable));

            PdfPCell resteLabel = new PdfPCell(new Phrase("Reste à Payer", fontTableHeader));
            resteLabel.setHorizontalAlignment(Element.ALIGN_LEFT);
            resteLabel.setPadding(4f);
            resteLabel.setBackgroundColor(BaseColor.YELLOW);

            PdfPCell resteValue = new PdfPCell(new Phrase(String.format("%.0f F", resteAPayer), fontMontant));
            resteValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
            resteValue.setPadding(4f);
            resteValue.setBackgroundColor(BaseColor.YELLOW);

            totaux.addCell(resteLabel);
            totaux.addCell(resteValue);

            PdfPCell totauxCell = new PdfPCell(totaux);
            totauxCell.setBorder(Rectangle.NO_BORDER);

            outer.addCell(totauxCell);
            document.add(outer);

            Paragraph signature = new Paragraph("Signature", fontInfo);
            signature.setAlignment(Element.ALIGN_RIGHT);
            signature.setSpacingBefore(10);
            document.add(signature);

            document.close();

            String pressingFolder = PDF_BASE_FOLDER + pressing.getNom().replaceAll("[^a-zA-Z0-9]", "_") + "/";
            Files.createDirectories(Paths.get(pressingFolder));
            String filePath = pressingFolder + "commande_" + commande.getId() + ".pdf";

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                fos.write(out.toByteArray());
            }

        } catch (Exception e) {
            throw new RuntimeException("Erreur PDF : " + e.getMessage(), e);
        }

        return out.toByteArray();
    }

    private PdfPCell createCellNoBorder(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(3f);
        return cell;
    }

    private PdfPCell createCellLeft(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(3f);
        return cell;
    }

    private PdfPCell createCellCenter(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(3f);
        return cell;
    }

    private PdfPCell createCellRight(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(3f);
        return cell;
    }

    private String formatNumeroFacture(Long id) {
        return String.format("%09d", id == null ? 0L : id);
    }
}
