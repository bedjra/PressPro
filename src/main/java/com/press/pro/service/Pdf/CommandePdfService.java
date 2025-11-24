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
            PdfPTable header = new PdfPTable(2);
            header.setWidthPercentage(100);
            header.setWidths(new float[]{1f, 3f});
            header.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            // LOGO
            PdfPCell logo = new PdfPCell();
            logo.setBorder(Rectangle.NO_BORDER);
            try {
                if (pressing.getLogo() != null && pressing.getLogo().length > 0) {
                    Image img = Image.getInstance(pressing.getLogo());
                    img.scaleToFit(40, 40);
                    logo.addElement(img);
                }
            } catch (Exception ignored) {}
            header.addCell(logo);

            // INFOS PRESSING
            PdfPCell info = new PdfPCell();
            info.setBorder(Rectangle.NO_BORDER);
            info.setPaddingLeft(5f);

            info.addElement(new Paragraph(pressing.getNom(), fontHeader));

            if (pressing.getAdresse() != null)
                info.addElement(new Paragraph(pressing.getAdresse(), fontNormal));

            if (pressing.getEmail() != null)
                info.addElement(new Paragraph(pressing.getEmail(), fontNormal));

            // Tél + Cel sur une seule ligne
            String contacts = "Tél: " + pressing.getTelephone();
            if (pressing.getCel() != null && !pressing.getCel().isEmpty())
                contacts += " | Cel: " + pressing.getCel();

            info.addElement(new Paragraph(contacts, fontNormal));

            header.addCell(info);

            document.add(header);
            document.add(Chunk.NEWLINE);

            // =======================
            //     N° DE REÇU
            // =======================
            Long numeroLocal = getNumeroLocal(commande);
            Paragraph recuNo = new Paragraph("Reçu N° " + formatNumeroFacture(numeroLocal), fontBold);
            recuNo.setAlignment(Element.ALIGN_CENTER);
            document.add(recuNo);
            document.add(Chunk.NEWLINE);

            // =======================
            // CLIENT + DATES
            // =======================
            PdfPTable clientDate = new PdfPTable(2);
            clientDate.setWidthPercentage(100);
            clientDate.setWidths(new float[]{2f, 1f});
            clientDate.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            // GAUCHE : Client
            PdfPCell left = new PdfPCell();
            left.setBorder(Rectangle.NO_BORDER);
            left.addElement(new Paragraph("Client : " +
                    (client != null ? client.getNom() : "CLIENT DIVERS"), fontNormal));

            left.addElement(new Paragraph("Adresse : " +
                    (client != null && client.getAdresse() != null ? client.getAdresse() : "-"), fontNormal));

            clientDate.addCell(left);

            // DROITE : Dates
            DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            PdfPCell right = new PdfPCell();
            right.setBorder(Rectangle.NO_BORDER);
            right.addElement(new Paragraph("Réception : " +
                    (commande.getDateReception() != null ? commande.getDateReception().format(df) : "-"), fontNormal));

            right.addElement(new Paragraph("Livraison : " +
                    (commande.getDateLivraison() != null ? commande.getDateLivraison().format(df) : "-"), fontNormal));

            clientDate.addCell(right);

            document.add(clientDate);
            document.add(Chunk.NEWLINE);

            // =======================
            //   LIGNES COMMANDE
            // =======================
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{4f, 1f, 1f, 1f, 1f});

            addTableHeader(table, new String[]{"Article", "Qté", "P.U", "Montant", "Remise"}, fontBold);

            String ab = param != null ? abregerService(param.getService()) : "-";
            String article = param != null ? param.getArticle() : "-";
            String articleFinal = article + " (" + ab + ")";

            double prix = param != null ? param.getPrix() : 0;
            int qte = commande.getQte();
            double remise = commande.getRemise();
            double montant = prix * qte;

            table.addCell(createCellLeft(articleFinal, fontNormal));
            table.addCell(createCellCenter(String.valueOf(qte), fontNormal));
            table.addCell(createCellRight(String.format("%.0f F", prix), fontNormal));
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
            // SIGNATURE + MERCI
            // =======================
            Paragraph sign = new Paragraph("Signature", fontNormal);
            sign.setAlignment(Element.ALIGN_RIGHT);
            document.add(sign);

            Paragraph merci = new Paragraph("Merci pour votre confiance!", fontNormal);
            merci.setAlignment(Element.ALIGN_CENTER);
            document.add(merci);

            document.close();

            // SAVE PDF
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
    private String abregerService(String s) {
        if (s == null || s.isBlank()) return "-";
        s = s.toUpperCase().replace("+", " ");
        String[] mots = s.split("\\s+");
        StringBuilder a = new StringBuilder();
        for (String m : mots) {
            if (!m.isBlank()) a.append(m.charAt(0));
            if (a.length() == 2) break;
        }
        return a.toString();
    }

    private long getNumeroLocal(Commande c) {
        Pressing p = c.getPressing();
        return p.getCommandes()
                .stream()
                .filter(x -> x.getId() <= c.getId())
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
