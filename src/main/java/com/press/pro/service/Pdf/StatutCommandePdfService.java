package com.press.pro.service.Pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.press.pro.Entity.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

@Service
public class StatutCommandePdfService {

    private static final String PDF_BASE_FOLDER = "pdfStatut/";

    public byte[] genererStatutPdf(Commande commande,
                                   double montantActuel,
                                   double montantAvant,
                                   Utilisateur utilisateur) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Rectangle receiptSize = new Rectangle(250, 700);
        Document document = new Document(receiptSize, 10, 10, 10, 10);

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Polices améliorées
            Font fontBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
            Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 8);
            Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font fontSmall = FontFactory.getFont(FontFactory.HELVETICA, 7);

            Pressing pressing = commande.getPressing();
            Client client = commande.getClient();

            // =======================
            //        ENTÊTE PRO
            // =======================
            PdfPTable header = new PdfPTable(2);
            header.setWidthPercentage(100);
            header.setWidths(new float[]{1.2f, 3f});
            header.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            header.setSpacingAfter(8f);

            // LOGO encadré
            PdfPCell logo = new PdfPCell();
            logo.setBorder(Rectangle.BOX);
            logo.setBorderWidth(0.5f);
            logo.setBorderColor(BaseColor.LIGHT_GRAY);
            logo.setVerticalAlignment(Element.ALIGN_MIDDLE);
            logo.setHorizontalAlignment(Element.ALIGN_CENTER);
            logo.setPadding(3);
            logo.setFixedHeight(65f);

            try {
                if (pressing.getLogo() != null && pressing.getLogo().length > 0) {
                    Image img = Image.getInstance(pressing.getLogo());
                    img.scaleToFit(55, 55);
                    img.setAlignment(Image.ALIGN_CENTER);
                    logo.addElement(img);
                }
            } catch (Exception ignored) {
                Paragraph noLogo = new Paragraph("LOGO", fontSmall);
                noLogo.setAlignment(Element.ALIGN_CENTER);
                logo.addElement(noLogo);
            }

            header.addCell(logo);

            // INFOS PRESSING stylisées
            PdfPCell info = new PdfPCell();
            info.setBorder(Rectangle.NO_BORDER);
            info.setPaddingLeft(8f);
            info.setVerticalAlignment(Element.ALIGN_MIDDLE);

            Paragraph nom = new Paragraph(pressing.getNom(), fontHeader);
            nom.setSpacingAfter(3f);
            info.addElement(nom);

            if (pressing.getAdresse() != null && !pressing.getAdresse().isEmpty()) {
                Paragraph adresse = new Paragraph(pressing.getAdresse(), fontSmall);
                adresse.setSpacingAfter(2f);
                info.addElement(adresse);
            }

            if (pressing.getEmail() != null && !pressing.getEmail().isEmpty()) {
                info.addElement(new Paragraph("✉ " + pressing.getEmail(), fontSmall));
            }

            String contacts = "☎ " + pressing.getTelephone();
            if (pressing.getCel() != null && !pressing.getCel().isEmpty())
                contacts += " | " + pressing.getCel();
            info.addElement(new Paragraph(contacts, fontSmall));

            header.addCell(info);
            document.add(header);

            addSeparator(document);

            // =======================
            //   TITRE STYLISÉ
            // =======================
            PdfPTable titreBox = new PdfPTable(1);
            titreBox.setWidthPercentage(100);

            PdfPCell tc = new PdfPCell();
            tc.setBackgroundColor(new BaseColor(240, 240, 240));
            tc.setBorder(Rectangle.BOX);
            tc.setBorderWidth(1f);
            tc.setPadding(5f);

            Paragraph titre = new Paragraph("BON DE LIVRAISON N° " + formatNumero(commande.getId()), fontTitle);
            titre.setAlignment(Element.ALIGN_CENTER);
            titre.setSpacingAfter(10f); // <-- ajoute l'espace en bas

            tc.addElement(titre);

            titreBox.addCell(tc);
            document.add(titreBox);

// =======================
//   CLIENT + DATES PRO
// =======================
            PdfPTable clientDate = new PdfPTable(2);
            clientDate.setWidthPercentage(100);
            clientDate.setSpacingBefore(10f); // <-- espace en haut
            clientDate.setSpacingAfter(10f);
            clientDate.setWidths(new float[]{2.2f, 1.8f});

            DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            PdfPCell left = styledBox("Client", fontBold);
            left.addElement(new Paragraph(client != null ? client.getNom() : "CLIENT DIVERS", fontNormal));

            if (client != null && client.getAdresse() != null)
                left.addElement(new Paragraph(client.getAdresse(), fontSmall));

            clientDate.addCell(left);

            PdfPCell right = styledBox("Dates", fontBold);
            right.addElement(new Paragraph("Réception: " +
                    (commande.getDateReception() != null ? commande.getDateReception().format(df) : "-"), fontSmall));

            right.addElement(new Paragraph("Livraison: " +
                    (commande.getDateLivraison() != null ? commande.getDateLivraison().format(df) : "-"), fontSmall));

            clientDate.addCell(right);

            document.add(clientDate);

            // =======================
            //     LIGNES PRODUITS
            // =======================
//            PdfPTable lignes = new PdfPTable(4);
//            lignes.setWidthPercentage(100);
//            lignes.setWidths(new float[]{3.5f, 0.8f, 1.2f, 1.5f});
//            addHeader(lignes, new String[]{"Article", "Qté", "P.U", "Montant"}, fontBold);
//
//            int index = 0;
//            for (CommandeLigne lg : commande.getLignes()) {
//
//                Parametre p = lg.getParametre();
//                String ab = p != null ? abregerService(p.getService()) : "-";
//                String art = p != null ? p.getArticle() : "-";
//                String tf = art + " (" + ab + ")";
//
//                double pu = p != null ? p.getPrix() : 0;
//                double mt = lg.getQuantite() * pu;
//
//                BaseColor bg = (index % 2 == 0) ? BaseColor.WHITE : new BaseColor(248, 248, 248);
//
//                lignes.addCell(styled(tf, fontNormal, Element.ALIGN_LEFT, bg));
//                lignes.addCell(styled(String.valueOf(lg.getQuantite()), fontNormal, Element.ALIGN_CENTER, bg));
//                lignes.addCell(styled(String.format("%.0f F", pu), fontNormal, Element.ALIGN_RIGHT, bg));
//                lignes.addCell(styled(String.format("%.0f F", mt), fontBold, Element.ALIGN_RIGHT, bg));
//
//                index++;
//            }
//
//            document.add(lignes);
//            addSeparator(document);

            // =======================
            //       PAIEMENTS
            // =======================
            PdfPTable pay = new PdfPTable(2);
            pay.setWidthPercentage(100);
            pay.setSpacingAfter(10f);

            pay.addCell(totalCell("Montant payé avant", fontNormal, false));
            pay.addCell(totalCell(String.format("%.0f F", montantAvant), fontNormal, true));

            pay.addCell(totalCell("Montant payé maintenant", fontNormal, false));
            pay.addCell(totalCell(String.format("%.0f F", montantActuel), fontNormal, true));

            double total = montantAvant + montantActuel;
            double net = commande.getMontantNetTotal() - commande.getRemise();
            double reste = net - total;

            pay.addCell(totalCell("Total payé", fontBold, false, new BaseColor(230, 255, 230)));
            pay.addCell(totalCell(String.format("%.0f F", total), fontBold, true, new BaseColor(230, 255, 230)));

            BaseColor resteBg = reste > 0 ? new BaseColor(255, 245, 230) : new BaseColor(230, 255, 230);
            pay.addCell(totalCell("Reste à payer", fontBold, false, resteBg));
            pay.addCell(totalCell(String.format("%.0f F", reste), fontBold, true, resteBg));

            document.add(pay);

            addSeparator(document);

            // =======================
            // Message final
            // =======================
            Paragraph merci = new Paragraph("Merci pour votre fidélité !", fontTitle);
            merci.setAlignment(Element.ALIGN_CENTER);
            merci.setSpacingBefore(10f);
            document.add(merci);

            Paragraph userInfo = new Paragraph(
                    "Émis par : " + utilisateur.getEmail() + " " + utilisateur.getRole(),
                    fontSmall
            );
            userInfo.setAlignment(Element.ALIGN_CENTER);
            userInfo.setSpacingBefore(5f);
            document.add(userInfo);


            document.close();

            // SAVE
            String folder = PDF_BASE_FOLDER + pressing.getNom().replaceAll("[^a-zA-Z0-9]", "_") + "/";
            Files.createDirectories(Paths.get(folder));
            try (FileOutputStream fos = new FileOutputStream(folder + "statut_" + commande.getId() + ".pdf")) {
                fos.write(out.toByteArray());
            }

        } catch (Exception e) {
            throw new RuntimeException("Erreur PDF : " + e.getMessage(), e);
        }

        return out.toByteArray();
    }

    /* ======= UTILITAIRES DESIGN ======= */

    private void addSeparator(Document doc) throws DocumentException {
        LineSeparator line = new LineSeparator();
        line.setLineWidth(1f);
        line.setLineColor(BaseColor.LIGHT_GRAY);
        doc.add(new Chunk(line));
        doc.add(Chunk.NEWLINE);
    }

    private PdfPCell styledBox(String title, Font font) {
        PdfPCell c = new PdfPCell();
        c.setBorder(Rectangle.BOX);
        c.setBorderColor(BaseColor.LIGHT_GRAY);
        c.setPadding(5f);
        c.setBackgroundColor(new BaseColor(250, 250, 250));

        Paragraph p = new Paragraph(title, font);
        p.setSpacingAfter(3f);
        c.addElement(p);

        return c;
    }

    private void addHeader(PdfPTable table, String[] headers, Font font) {
        for (String h : headers) {
            PdfPCell c = new PdfPCell(new Phrase(h, font));
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            c.setBackgroundColor(new BaseColor(200, 200, 200));
            c.setPadding(4f);
            c.setBorder(Rectangle.BOX);
            c.setBorderWidth(0.5f);
            table.addCell(c);
        }
    }

    private PdfPCell styled(String txt, Font f, int align, BaseColor bg) {
        PdfPCell c = new PdfPCell(new Phrase(txt, f));
        c.setHorizontalAlignment(align);
        c.setBackgroundColor(bg);
        c.setBorder(Rectangle.NO_BORDER);
        c.setPadding(4f);
        return c;
    }

    private PdfPCell totalCell(String txt, Font f, boolean amount) {
        return totalCell(txt, f, amount, null);
    }

    private PdfPCell totalCell(String txt, Font f, boolean amount, BaseColor bg) {
        PdfPCell c = new PdfPCell(new Phrase(txt, f));
        c.setHorizontalAlignment(amount ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT);
        c.setBorder(Rectangle.NO_BORDER);
        c.setPadding(4f);
        if (bg != null) c.setBackgroundColor(bg);
        return c;
    }

    private String abregerService(String s) {
        if (s == null || s.isBlank()) return "-";
        s = s.toUpperCase().replace("+", " ");
        String[] mots = s.split("\\s+");
        StringBuilder a = new StringBuilder();
        for (String m : mots) {
            if (!m.isBlank()) a.append(m.charAt(0));
            if (a.length() == 3) break;
        }
        return a.toString();
    }

    private String formatNumero(Long id) {
        return String.format("%09d", id == null ? 0 : id);
    }
}
