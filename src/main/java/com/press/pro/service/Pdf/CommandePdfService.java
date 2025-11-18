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

@Service
public class CommandePdfService {

    private static final String PDF_BASE_FOLDER = "pdfCommandes/";

    public byte[] genererCommandePdf(Commande commande) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A6, 10, 10, 10, 10);

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // --------------------- FONTS ---------------------
            Font fontTitre = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font fontSousTitre = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font fontInfo = FontFactory.getFont(FontFactory.HELVETICA, 7);
            Font fontTableHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7);
            Font fontTable = FontFactory.getFont(FontFactory.HELVETICA, 7);
            Font fontMontant = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);

            Pressing pressing = commande.getPressing();

            // --------------------- ENTÊTE ---------------------
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{1f, 3f});
            headerTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            // Logo
            PdfPCell logoCell = new PdfPCell();
            logoCell.setBorder(Rectangle.NO_BORDER);
            if (pressing.getLogo() != null) {
                try {
                    Image logo = Image.getInstance(pressing.getLogo());
                    logo.scaleToFit(40, 40);
                    logoCell.addElement(logo);
                } catch (Exception ignored) {}
            }
            headerTable.addCell(logoCell);

            // Infos pressing
            PdfPCell infoCell = new PdfPCell();
            infoCell.setBorder(Rectangle.NO_BORDER);
            infoCell.addElement(new Paragraph(pressing.getNom(), fontTitre));
            infoCell.addElement(new Paragraph(pressing.getAdresse(), fontInfo));
            infoCell.addElement(new Paragraph("Tél: " + pressing.getTelephone(), fontInfo));
            if (pressing.getEmail() != null)
                infoCell.addElement(new Paragraph(pressing.getEmail(), fontInfo));

            headerTable.addCell(infoCell);
            document.add(headerTable);

            document.add(Chunk.NEWLINE);
            document.add(new LineSeparator());
            document.add(Chunk.NEWLINE);

            // Numéro Reçu
            Paragraph factureTitle = new Paragraph(
                    "Reçu N° " + String.format("%09d", commande.getId()),
                    fontSousTitre
            );
            factureTitle.setAlignment(Element.ALIGN_CENTER);
            factureTitle.setSpacingAfter(5);
            document.add(factureTitle);

            // --------------------- INFOS CLIENT ---------------------
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setWidths(new float[]{2f, 1f});
            infoTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            Client client = commande.getClient();

            // Colonne gauche
            PdfPTable left = new PdfPTable(1);
            left.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            left.addCell(noBorder("Client : " + (client != null ? client.getNom() : "CLIENT DIVERS"), fontTable));
            left.addCell(noBorder("Adresse : " + (client != null ? client.getAdresse() : "-"), fontInfo));
            infoTable.addCell(left);

            // Colonne droite
            PdfPTable right = new PdfPTable(1);
            right.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String date = commande.getDateReception() != null ? commande.getDateReception().format(fmt) : "";
            right.addCell(noBorder("Date : " + date, fontInfo));
            infoTable.addCell(right);

            document.add(infoTable);
            document.add(Chunk.NEWLINE);

            // --------------------- TABLEAU ARTICLES ---------------------
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{0.7f, 3f, 1.1f, 1f, 1.1f, 1.3f});

            String[] headers = {"Qté", "Désignation", "P.V.U", "Remise", "Net", "Montant"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, fontTableHeader));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                cell.setPadding(3f);
                table.addCell(cell);
            }

            Parametre param = commande.getParametre();
            String description = param != null ? param.getArticle() : "-";
            double prixUnitaire = param != null ? param.getPrix() : 0;
            int qte = commande.getQte();
            double remiseTotale = commande.getRemise();
            double montantHT = prixUnitaire * qte;

            table.addCell(center(String.valueOf(qte), fontTable));
            table.addCell(left(description, fontTable));
            table.addCell(right(String.format("%.0f F", prixUnitaire), fontTable));
            table.addCell(right(String.format("%.0f F", remiseTotale), fontTable));
            table.addCell(right(String.format("%.0f F", prixUnitaire), fontTable));
            table.addCell(right(String.format("%.0f F", montantHT), fontTable));

            document.add(table);

            // --------------------- TOTAUX PROPRE ---------------------
            PdfPTable totaux = new PdfPTable(2);
            totaux.setWidthPercentage(100);
            totaux.setWidths(new float[]{2f, 1f});

            // Méthodes locales pour styliser
            java.util.function.Function<String, PdfPCell> lbl = txt -> bordered(txt, fontTableHeader, Element.ALIGN_LEFT);
            java.util.function.Function<String, PdfPCell> val = txt -> bordered(txt, fontMontant, Element.ALIGN_RIGHT);

            double netCommercial = montantHT - remiseTotale;
            double montantTTC = netCommercial;
            double montantPaye = commande.getMontantPaye();
            double resteAPayer = montantTTC - montantPaye;

            // Remise
            totaux.addCell(lbl.apply("Remise"));
            totaux.addCell(val.apply(String.format("%.0f F", remiseTotale)));

            // Net commercial
            totaux.addCell(lbl.apply("Net Commercial"));
            totaux.addCell(val.apply(String.format("%.0f F", netCommercial)));

            // Montant TTC
            PdfPCell ttcLbl = lbl.apply("Montant TTC");
            ttcLbl.setBackgroundColor(BaseColor.LIGHT_GRAY);

            PdfPCell ttcVal = val.apply(String.format("%.0f F", montantTTC));
            ttcVal.setBackgroundColor(BaseColor.LIGHT_GRAY);

            totaux.addCell(ttcLbl);
            totaux.addCell(ttcVal);

            // Montant payé
            totaux.addCell(lbl.apply("Montant Payé"));
            totaux.addCell(val.apply(String.format("%.0f F", montantPaye)));

            // Reste à payer
            PdfPCell resteLbl = lbl.apply("Reste à Payer");
            resteLbl.setBackgroundColor(BaseColor.YELLOW);

            PdfPCell resteVal = val.apply(String.format("%.0f F", resteAPayer));
            resteVal.setBackgroundColor(BaseColor.YELLOW);

            totaux.addCell(resteLbl);
            totaux.addCell(resteVal);

            document.add(totaux);

            // Signature
            Paragraph signature = new Paragraph("Signature", fontInfo);
            signature.setAlignment(Element.ALIGN_RIGHT);
            signature.setSpacingBefore(10);
            document.add(signature);

            document.close();

            // --------------------- SAUVEGARDE ---------------------
            String folder = PDF_BASE_FOLDER + pressing.getNom().replaceAll("[^a-zA-Z0-9]", "_") + "/";
            Files.createDirectories(Paths.get(folder));
            try (FileOutputStream fos = new FileOutputStream(folder + "commande_" + commande.getId() + ".pdf")) {
                fos.write(out.toByteArray());
            }

        } catch (Exception e) {
            throw new RuntimeException("Erreur PDF : " + e.getMessage(), e);
        }

        return out.toByteArray();
    }

    // -----------------------------------------------------
    // 🔹 UTILITAIRES DE CELLES
    // -----------------------------------------------------

    private PdfPCell noBorder(String txt, Font f) {
        PdfPCell c = new PdfPCell(new Phrase(txt, f));
        c.setBorder(Rectangle.NO_BORDER);
        c.setPadding(2f);
        return c;
    }

    private PdfPCell left(String txt, Font f) {
        PdfPCell c = new PdfPCell(new Phrase(txt, f));
        c.setHorizontalAlignment(Element.ALIGN_LEFT);
        c.setBorder(Rectangle.NO_BORDER);
        c.setPadding(3f);
        return c;
    }

    private PdfPCell center(String txt, Font f) {
        PdfPCell c = new PdfPCell(new Phrase(txt, f));
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        c.setBorder(Rectangle.NO_BORDER);
        c.setPadding(3f);
        return c;
    }

    private PdfPCell right(String txt, Font f) {
        PdfPCell c = new PdfPCell(new Phrase(txt, f));
        c.setHorizontalAlignment(Element.ALIGN_RIGHT);
        c.setBorder(Rectangle.NO_BORDER);
        c.setPadding(3f);
        return c;
    }

    private static PdfPCell bordered(String txt, Font f, int align) {
        PdfPCell c = new PdfPCell(new Phrase(txt, f));
        c.setHorizontalAlignment(align);
        c.setPadding(4f);
        c.setBorder(Rectangle.BOX);
        return c;
    }
}
