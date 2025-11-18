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
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);

        try {

            PdfWriter.getInstance(document, out);
            document.open();

            // Styles
            Font fontTitre = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font fontSousTitre = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font fontInfo = FontFactory.getFont(FontFactory.HELVETICA, 9);
            Font fontTableHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
            Font fontTable = FontFactory.getFont(FontFactory.HELVETICA, 9);
            Font fontMontant = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);

            Pressing pressing = commande.getPressing();

            // ---------------------------
            //        ENTETE
            // ---------------------------
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{1f, 3f});
            headerTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            // ---- LOGO ----
            PdfPCell logoCell = new PdfPCell();
            logoCell.setBorder(Rectangle.NO_BORDER);
            logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

            try {
                if (pressing.getLogo() != null && pressing.getLogo().length > 0) {

                    Image logo = Image.getInstance(pressing.getLogo());
                    logo.scaleToFit(70, 70);
                    logoCell.addElement(logo);
                }

            } catch (Exception e) {
                System.out.println("Erreur chargement logo : " + e.getMessage());
            }

            headerTable.addCell(logoCell);

            // ---- INFOS PRESSING ----
            PdfPCell infoCell = new PdfPCell();
            infoCell.setBorder(Rectangle.NO_BORDER);
            infoCell.setPaddingLeft(10f);

            Paragraph nom = new Paragraph(pressing.getNom(), fontTitre);
            nom.setAlignment(Element.ALIGN_LEFT);

            infoCell.addElement(nom);
            infoCell.addElement(new Paragraph(pressing.getAdresse(), fontInfo));
            infoCell.addElement(new Paragraph("Tél: " + pressing.getTelephone(), fontInfo));

            if (pressing.getEmail() != null)
                infoCell.addElement(new Paragraph(pressing.getEmail(), fontInfo));

            headerTable.addCell(infoCell);
            document.add(headerTable);

            // Ligne séparatrice
            document.add(Chunk.NEWLINE);
            document.add(new LineSeparator());
            document.add(Chunk.NEWLINE);

            // ---------------------------
            //      TITRE FACTURE
            // ---------------------------
            Paragraph factureTitle = new Paragraph(
                    "Reçu N° " + formatNumeroFacture(commande.getId()),
                    fontSousTitre
            );
            factureTitle.setAlignment(Element.ALIGN_CENTER);
            factureTitle.setSpacingAfter(8);
            document.add(factureTitle);

            // ---------------------------
            //      INFO CLIENT
            // ---------------------------
            PdfPTable topInfo = new PdfPTable(2);
            topInfo.setWidthPercentage(100);
            topInfo.setWidths(new float[]{2f, 1f});
            topInfo.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            Client client = commande.getClient();

            PdfPTable left = new PdfPTable(1);
            left.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            left.addCell(createCellNoBorder("NOM DU CLIENT : " +
                    (client != null ? client.getNom() : "CLIENT DIVERS"), fontTable));

            left.addCell(createCellNoBorder("Adresse : " +
                    (client != null && client.getAdresse() != null ? client.getAdresse() : "-"), fontInfo));

            topInfo.addCell(left);

            PdfPTable right = new PdfPTable(1);
            right.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String dateFact = commande.getDateReception() != null ?
                    commande.getDateReception().format(formatter) : "";

            right.addCell(createCellNoBorder("Date Facture : " + dateFact, fontInfo));
            right.addCell(createCellNoBorder("", fontInfo));

            topInfo.addCell(right);
            document.add(topInfo);
            document.add(Chunk.NEWLINE);

            // ---------------------------
            //      TABLEAU ARTICLES
            // ---------------------------
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{0.8f, 4f, 1.2f, 1f, 1.2f, 1.4f});
            table.setSpacingAfter(6f);

            Stream.of("Qté", "Désignation", "P.V.U", "Remise", "Prix Net", "Montant")
                    .forEach(h -> {
                        PdfPCell hd = new PdfPCell(new Phrase(h, fontTableHeader));
                        hd.setHorizontalAlignment(Element.ALIGN_CENTER);
                        hd.setPadding(6f);
                        hd.setBackgroundColor(BaseColor.LIGHT_GRAY);
                        table.addCell(hd);
                    });

            Parametre param = commande.getParametre();
            String description =
                    param != null ?
                            param.getArticle() +
                                    ((param.getService() != null && !param.getService().isBlank())
                                            ? " / " + param.getService()
                                            : "")
                            : "-";

            double prixUnitaire = param != null ? param.getPrix() : 0.0;

            int qte = commande.getQte();
            double remiseTotale = commande.getRemise();

            // --- CALCULS CORRIGÉS ---
            double montantHT = prixUnitaire * qte;
            double prixNet = prixUnitaire;
            double montantAffiche = montantHT;

            // --- AJOUT LIGNE TABLEAU ---
            table.addCell(createCellCenter(String.valueOf(qte), fontTable));
            table.addCell(createCellLeft(description, fontTable));
            table.addCell(createCellRight(String.format("%.0f F", prixUnitaire), fontTable));
            table.addCell(createCellRight(String.format("%.0f F", remiseTotale), fontTable));
            table.addCell(createCellRight(String.format("%.0f F", prixNet), fontTable));
            table.addCell(createCellRight(String.format("%.0f F", montantAffiche), fontTable));

            document.add(table);

            // ---------------------------
            //          TOTAUX
            // ---------------------------
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

            // --- CALCULS TOTAUX CORRIGÉS ---
            double montantTotalHT = montantHT;
            double netCommercial = montantTotalHT - remiseTotale;
            double montantTVA = 0;
            double montantTTC = netCommercial;

            double montantPaye = commande.getMontantPaye();
            double resteAPayer = montantTTC - montantPaye;

            // ---- TOTAUX ----
            totaux.addCell(createCellRight("Montant Total HT", fontTableHeader));
            totaux.addCell(createCellRight(String.format("%.0f F", montantTotalHT), fontTable));

            totaux.addCell(createCellRight("Remise", fontTableHeader));
            totaux.addCell(createCellRight(String.format("%.0f F", remiseTotale), fontTable));

            totaux.addCell(createCellRight("Net Commercial", fontTableHeader));
            totaux.addCell(createCellRight(String.format("%.0f F", netCommercial), fontTable));

            totaux.addCell(createCellRight("Montant TVA", fontTableHeader));
            totaux.addCell(createCellRight(String.format("%.0f F", montantTVA), fontTable));

            PdfPCell ttcLabel = new PdfPCell(new Phrase("Montant TTC", fontTableHeader));
            ttcLabel.setHorizontalAlignment(Element.ALIGN_LEFT);
            ttcLabel.setPadding(6f);
            ttcLabel.setBackgroundColor(BaseColor.LIGHT_GRAY);

            PdfPCell ttcValue = new PdfPCell(new Phrase(String.format("%.0f F", montantTTC), fontMontant));
            ttcValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
            ttcValue.setPadding(6f);
            ttcValue.setBackgroundColor(BaseColor.LIGHT_GRAY);

            totaux.addCell(ttcLabel);
            totaux.addCell(ttcValue);

            totaux.addCell(createCellRight("Montant Payé", fontTableHeader));
            totaux.addCell(createCellRight(String.format("%.0f F", montantPaye), fontTable));

            PdfPCell resteLabel = new PdfPCell(new Phrase("Reste à Payer", fontTableHeader));
            resteLabel.setHorizontalAlignment(Element.ALIGN_LEFT);
            resteLabel.setPadding(6f);
            resteLabel.setBackgroundColor(BaseColor.YELLOW);

            PdfPCell resteValue = new PdfPCell(new Phrase(String.format("%.0f F", resteAPayer), fontMontant));
            resteValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
            resteValue.setPadding(6f);
            resteValue.setBackgroundColor(BaseColor.YELLOW);

            totaux.addCell(resteLabel);
            totaux.addCell(resteValue);

            PdfPCell totauxCell = new PdfPCell(totaux);
            totauxCell.setBorder(Rectangle.NO_BORDER);

            outer.addCell(totauxCell);
            document.add(outer);

            // ---------------------------
            //      ARRETE & SIGNATURE
            // ---------------------------
            Paragraph signature = new Paragraph("Signature", fontInfo);
            signature.setAlignment(Element.ALIGN_RIGHT);
            signature.setSpacingBefore(40);
            document.add(signature);

            document.close();

            // ---------------------------
            //      SAUVEGARDE FICHIER
            // ---------------------------
            String pressingName = pressing.getNom() != null ?
                    pressing.getNom().replaceAll("[^a-zA-Z0-9]", "_") :
                    "pressing";

            String pressingFolder = PDF_BASE_FOLDER + pressingName + "/";
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

    // ==== UTILITAIRES ====

    private PdfPCell createCellNoBorder(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(4f);
        return cell;
    }

    private PdfPCell createCellLeft(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(5f);
        return cell;
    }

    private PdfPCell createCellCenter(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(5f);
        return cell;
    }

    private PdfPCell createCellRight(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(5f);
        return cell;
    }

    private String formatNumeroFacture(Long id) {
        if (id == null) id = 0L;
        return String.format("%09d", id);
    }

    private String convertNumberToFrenchWords(long number) {
        if (number == 0) return "zéro";
        if (number < 0) return "moins " + convertNumberToFrenchWords(-number);

        String[] units = {"", "un", "deux", "trois", "quatre", "cinq", "six", "sept", "huit", "neuf", "dix",
                "onze", "douze", "treize", "quatorze", "quinze", "seize", "dix-sept", "dix-huit", "dix-neuf"};

        String[] tens = {"", "", "vingt", "trente", "quarante", "cinquante", "soixante", "soixante",
                "quatre-vingt", "quatre-vingt"};

        StringBuilder words = new StringBuilder();

        if (number >= 1_000_000) {
            long millions = number / 1_000_000;
            words.append(convertNumberToFrenchWords(millions)).append(" million");
            if (millions > 1) words.append("s");
            number %= 1_000_000;
            if (number > 0) words.append(" ");
        }

        if (number >= 1000) {
            long thousands = number / 1000;
            if (thousands > 1) words.append(convertNumberToFrenchWords(thousands)).append(" ");
            words.append("mille");
            number %= 1000;
            if (number > 0) words.append(" ");
        }

        if (number >= 100) {
            long hundreds = number / 100;
            if (hundreds > 1) words.append(units[(int) hundreds]).append(" ");
            words.append("cent");
            number %= 100;
            if (hundreds > 1 && number == 0) words.append("s");
            if (number > 0) words.append(" ");
        }

        if (number > 0 && number < 20) {
            words.append(units[(int) number]);
        } else if (number >= 20) {
            int ten = (int) number / 10;
            int unit = (int) number % 10;

            if (ten == 7 || ten == 9) {
                int base = ten == 7 ? 6 : 8;
                words.append(tens[base]);
                int remainder = (int) (number - base * 10);
                if (remainder > 0) {
                    words.append("-");
                    words.append(units[remainder]);
                }
            } else {
                words.append(tens[ten]);
                if (unit > 0) {
                    if (unit == 1 && (ten == 2 || ten == 3 || ten == 4 || ten == 5 || ten == 6)) {
                        words.append("-et-un");
                    } else {
                        words.append("-").append(units[unit]);
                    }
                }
            }
        }

        return words.toString().trim();
    }
}
