package com.press.pro.service.Pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
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

        Rectangle receiptSize = new Rectangle(226, 2000); // Ticket 58mm
        Document document = new Document(receiptSize, 5, 5, 5, 5);

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font fontBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
            Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 7);
            Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

            Pressing pressing = commande.getPressing();
            Client client = commande.getClient();
            Parametre param = commande.getParametre();

            // =======================
            //        ENTETE
            // =======================
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{1f, 3f});
            headerTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            PdfPCell logoCell = new PdfPCell();
            logoCell.setBorder(Rectangle.NO_BORDER);
            try {
                if (pressing.getLogo() != null && pressing.getLogo().length > 0) {
                    Image logo = Image.getInstance(pressing.getLogo());
                    logo.scaleToFit(40, 40);
                    logoCell.addElement(logo);
                }
            } catch (Exception ignored) {}
            headerTable.addCell(logoCell);

            PdfPCell info = new PdfPCell();
            info.setBorder(Rectangle.NO_BORDER);
            info.setPaddingLeft(5f);
            info.addElement(new Paragraph(pressing.getNom(), fontHeader));
            if (pressing.getAdresse() != null) info.addElement(new Paragraph(pressing.getAdresse(), fontNormal));
            info.addElement(new Paragraph("Tél: " + pressing.getTelephone(), fontNormal));
            if (pressing.getCel() != null) info.addElement(new Paragraph("Cel: " + pressing.getCel(), fontNormal));
            if (pressing.getEmail() != null) info.addElement(new Paragraph(pressing.getEmail(), fontNormal));
            headerTable.addCell(info);

            document.add(headerTable);
            document.add(Chunk.NEWLINE);

            // =======================
            //     NUMERO DE REÇU
            // =======================
            Long numeroLocal = getNumeroLocal(commande);
            Paragraph receiptNumber = new Paragraph("Reçu N° " + formatNumeroFacture(numeroLocal), fontBold);
            receiptNumber.setAlignment(Element.ALIGN_CENTER);
            document.add(receiptNumber);
            document.add(Chunk.NEWLINE);

            // =======================
            //     INFO CLIENT
            // =======================
            PdfPTable clientTable = new PdfPTable(1);
            clientTable.setWidthPercentage(100);

            clientTable.addCell(createCellNoBorder(
                    "Client : " + (client != null ? client.getNom() : "CLIENT DIVERS"), fontNormal));

            clientTable.addCell(createCellNoBorder(
                    "Adresse : " +
                            (client != null && client.getAdresse() != null ? client.getAdresse() : "-"), fontNormal));

            document.add(clientTable);

            DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            document.add(createParagraph("Réception : " +
                    (commande.getDateReception() != null ? commande.getDateReception().format(df) : "-"), fontNormal));

            document.add(createParagraph("Livraison : " +
                    (commande.getDateLivraison() != null ? commande.getDateLivraison().format(df) : "-"), fontNormal));

            document.add(Chunk.NEWLINE);

            // =======================
            //     LIGNES COMMANDE
            // =======================
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{4f, 1f, 1f, 1f, 1f});

            addTableHeader(table, new String[]{"Article", "Qté", "P.U", "Montant", "Remise"}, fontBold);

            String serviceAbrege = param != null ? abregerService(param.getService()) : "-";
            String article = param != null ? param.getArticle() : "-";

            // Format final : Chemise (LS)
            String articleFinal = article + " (" + serviceAbrege + ")";

            double prixUnitaire = param != null ? param.getPrix() : 0.0;
            int qte = commande.getQte();
            double remise = commande.getRemise();
            double montant = prixUnitaire * qte;

            table.addCell(createCellLeft(articleFinal, fontNormal));
            table.addCell(createCellCenter(String.valueOf(qte), fontNormal));
            table.addCell(createCellRight(String.format("%.0f F", prixUnitaire), fontNormal));
            table.addCell(createCellRight(String.format("%.0f F", montant), fontNormal));
            table.addCell(createCellRight(String.format("%.0f F", remise), fontNormal));

            document.add(table);
            document.add(Chunk.NEWLINE);

            // =======================
            //        TOTAUX
            // =======================
            double net = montant - remise;
            double paye = commande.getMontantPaye();
            double reste = net - paye;

            PdfPTable totaux = new PdfPTable(2);
            totaux.setWidthPercentage(100);
            totaux.setWidths(new float[]{2f, 1f});

            totaux.addCell(createCellLeftWithBg("Montant HT", fontNormal, BaseColor.LIGHT_GRAY));
            totaux.addCell(createCellRightWithBg(String.format("%.0f F", net), fontNormal, BaseColor.LIGHT_GRAY));
            totaux.addCell(createCellLeft("Montant Payé", fontNormal));
            totaux.addCell(createCellRight(String.format("%.0f F", paye), fontNormal));
            totaux.addCell(createCellLeft("Reste à Payer", fontBold));
            totaux.addCell(createCellRight(String.format("%.0f F", reste), fontBold));

            document.add(totaux);
            document.add(Chunk.NEWLINE);

            // =======================
            //     SIGNATURE + MERCI
            // =======================
            Paragraph sign = new Paragraph("Signature", fontNormal);
            sign.setAlignment(Element.ALIGN_RIGHT);
            document.add(sign);

            Paragraph merci = new Paragraph("Merci pour votre confiance!", fontNormal);
            merci.setAlignment(Element.ALIGN_CENTER);
            document.add(merci);

            document.close();

            // =======================
            //   SAUVEGARDE PDF
            // =======================
            String folder = PDF_BASE_FOLDER +
                    pressing.getNom().replaceAll("[^a-zA-Z0-9]", "_") + "/";

            Files.createDirectories(Paths.get(folder));

            try (FileOutputStream fos = new FileOutputStream(folder + "commande_" + commande.getId() + ".pdf")) {
                fos.write(out.toByteArray());
            }

        } catch (Exception e) {
            throw new RuntimeException("Erreur PDF thermique : " + e.getMessage(), e);
        }

        return out.toByteArray();
    }

    // ==========================
    //       UTILITAIRES
    // ==========================
    private String abregerService(String service) {
        if (service == null || service.isBlank()) return "-";

        service = service.toUpperCase().replace("+", " ");
        String[] mots = service.split("\\s+");

        StringBuilder abrev = new StringBuilder();
        for (String mot : mots) {
            if (!mot.isBlank()) abrev.append(mot.charAt(0));
            if (abrev.length() == 2) break;
        }
        return abrev.toString();
    }

    private long getNumeroLocal(Commande commande) {
        Pressing pressing = commande.getPressing();
        return pressing.getCommandes()
                .stream()
                .filter(c -> c.getId() <= commande.getId())
                .count();
    }

    private String formatNumeroFacture(Long id) {
        return String.format("%09d", id == null ? 0L : id);
    }

    private PdfPCell createCellNoBorder(String text, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setBorder(Rectangle.NO_BORDER);
        return c;
    }

    private PdfPCell createCellLeft(String text, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setHorizontalAlignment(Element.ALIGN_LEFT);
        c.setBorder(Rectangle.NO_BORDER);
        return c;
    }

    private PdfPCell createCellLeftWithBg(String text, Font font, BaseColor bg) {
        PdfPCell c = createCellLeft(text, font);
        c.setBackgroundColor(bg);
        return c;
    }

    private PdfPCell createCellCenter(String text, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        c.setBorder(Rectangle.NO_BORDER);
        return c;
    }

    private PdfPCell createCellRight(String text, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setHorizontalAlignment(Element.ALIGN_RIGHT);
        c.setBorder(Rectangle.NO_BORDER);
        return c;
    }

    private PdfPCell createCellRightWithBg(String text, Font font, BaseColor bg) {
        PdfPCell c = createCellRight(text, font);
        c.setBackgroundColor(bg);
        return c;
    }

    private void addTableHeader(PdfPTable table, String[] headers, Font font) {
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, font));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(2f);
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            table.addCell(cell);
        }
    }

    private Paragraph createParagraph(String text, Font font) {
        return new Paragraph(text, font);
    }
}
