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
public class CommandePdfService {

    private static final String PDF_BASE_FOLDER = "pdfCommandes/";

    public byte[] genererCommandePdf(Commande commande, Utilisateur user) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(new Rectangle(250, 600), 10, 10, 10, 10);

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // ===================== FONTS =====================
            Font bold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
            Font normal = FontFactory.getFont(FontFactory.HELVETICA, 8);
            Font header = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font title = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font small = FontFactory.getFont(FontFactory.HELVETICA, 7);

            Pressing pressing = commande.getPressing();
            Client client = commande.getClient();

            // ===================== ENTÊTE =====================
            PdfPTable entete = new PdfPTable(2);
            entete.setWidthPercentage(100);
            entete.setWidths(new float[]{1.2f, 3f});
            entete.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            // Logo
            PdfPCell logo = new PdfPCell();
            logo.setBorder(Rectangle.BOX);
            logo.setPadding(3);
            logo.setFixedHeight(60f);
            logo.setHorizontalAlignment(Element.ALIGN_CENTER);
            if (pressing.getLogo() != null && pressing.getLogo().length > 0) {
                Image img = Image.getInstance(pressing.getLogo());
                img.scaleToFit(55, 55);
                logo.addElement(img);
            } else {
                logo.addElement(new Paragraph("LOGO", small));
            }
            entete.addCell(logo);

            // Infos pressing
            PdfPCell infos = new PdfPCell();
            infos.setBorder(Rectangle.NO_BORDER);
            infos.addElement(new Paragraph(pressing.getNom(), header));
            if (pressing.getAdresse() != null) infos.addElement(new Paragraph(pressing.getAdresse(), small));
            String tel = "☎ " + pressing.getTelephone();
            if (pressing.getCel() != null && !pressing.getCel().isEmpty()) tel += " | " + pressing.getCel();
            infos.addElement(new Paragraph(tel, small));
            entete.addCell(infos);
            document.add(entete);

            addSeparator(document);

            // ===================== NUMÉRO =====================
            Paragraph numero = new Paragraph(
                    "BON DE COMMANDE N° " + formatNumeroFacture(getNumeroLocal(commande)),
                    title
            );
            numero.setAlignment(Element.ALIGN_CENTER);
            numero.setSpacingAfter(5f);
            document.add(numero);

            // ===================== CLIENT / DATES =====================
            DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            PdfPTable infosClient = new PdfPTable(2);
            infosClient.setWidthPercentage(100);

            infosClient.addCell(cellBox("Client\n" + (client != null ? client.getNom() : "CLIENT DIVERS"), normal));
            infosClient.addCell(cellBox(
                    "Réception : " + (commande.getDateReception() != null ? commande.getDateReception().format(df) : "-") +
                            "\nLivraison : " + (commande.getDateLivraison() != null ? commande.getDateLivraison().format(df) : "-"),
                    small));
            document.add(infosClient);

            // ===================== LIGNES =====================
            PdfPTable lignes = new PdfPTable(4);
            lignes.setWidthPercentage(100);
            lignes.setWidths(new float[]{3.5f, 0.8f, 1.2f, 1.5f});
            addHeader(lignes, new String[]{"Article", "Qté", "P.U", "Montant"}, bold);

            for (CommandeLigne l : commande.getLignes()) {
                String article = l.getParametre() != null
                        ? l.getParametre().getArticle()
                        : l.getTarifKilo() != null ? l.getTarifKilo().getTranchePoids() : "-";

                double qte = l.getQuantite() != null ? l.getQuantite() : (l.getPoids() != null ? l.getPoids() : 0);
                double pu = l.getParametre() != null ? l.getParametre().getPrix() :
                        l.getTarifKilo() != null ? l.getTarifKilo().getPrix() : 0;

                lignes.addCell(cell(article, normal, Element.ALIGN_LEFT));
                lignes.addCell(cell(String.valueOf(qte), normal, Element.ALIGN_CENTER));
                lignes.addCell(cell(String.format("%.0f F", pu), normal, Element.ALIGN_RIGHT));
                lignes.addCell(cell(String.format("%.0f F", l.getMontantBrut()), bold, Element.ALIGN_RIGHT));
            }
            document.add(lignes);

            // ===================== TOTAUX =====================
            double totalBrut = commande.getLignes().stream()
                    .mapToDouble(CommandeLigne::getMontantBrut)
                    .sum();
            double remise = commande.getRemise();
            double netAPayer = totalBrut - remise;
            double paye = commande.getMontantPaye();
            double resteAPayer = Math.max(0, netAPayer - paye);

            PdfPTable totaux = new PdfPTable(2);
            totaux.setWidthPercentage(100);

            addTotal(totaux, "Montant brut", totalBrut, normal);
            if (remise > 0) addTotal(totaux, "Remise", remise, normal);
            addTotal(totaux, "Net à payer", netAPayer, bold);
            if (paye > 0) addTotal(totaux, "Payé", paye, normal);
            BaseColor bg = resteAPayer > 0 ? new BaseColor(255, 245, 230) : new BaseColor(230, 255, 230);
            addTotal(totaux, "Reste à payer", resteAPayer, bold, bg);
            document.add(totaux);

            // ===================== FOOTER =====================
            Paragraph footer1 = new Paragraph("Émis par : " + user.getEmail(), small);
            footer1.setAlignment(Element.ALIGN_CENTER);
            document.add(footer1);

            Paragraph footer2 = new Paragraph("Merci pour votre confiance", title);
            footer2.setAlignment(Element.ALIGN_CENTER);
            document.add(footer2);

            document.close();

            Files.createDirectories(Paths.get(PDF_BASE_FOLDER));
            try (FileOutputStream fos = new FileOutputStream(
                    PDF_BASE_FOLDER + "commande_" + commande.getId() + ".pdf")) {
                fos.write(out.toByteArray());
            }

        } catch (Exception e) {
            throw new RuntimeException("Erreur PDF : " + e.getMessage(), e);
        }

        return out.toByteArray();
    }

    // ===================== UTILITAIRES =====================
    private void addSeparator(Document doc) throws DocumentException {
        LineSeparator l = new LineSeparator();
        l.setLineColor(BaseColor.LIGHT_GRAY);
        doc.add(l);
    }

    private PdfPCell cell(String txt, Font f, int align) {
        PdfPCell c = new PdfPCell(new Phrase(txt, f));
        c.setBorder(Rectangle.NO_BORDER);
        c.setHorizontalAlignment(align);
        c.setPadding(4);
        return c;
    }

    private PdfPCell cellBox(String txt, Font f) {
        PdfPCell c = new PdfPCell(new Phrase(txt, f));
        c.setPadding(4);
        return c;
    }

    private void addHeader(PdfPTable t, String[] h, Font f) {
        for (String s : h) {
            PdfPCell c = new PdfPCell(new Phrase(s, f));
            c.setBackgroundColor(new BaseColor(200, 200, 200));
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            t.addCell(c);
        }
    }

    private void addTotal(PdfPTable t, String label, double val, Font f) {
        addTotal(t, label, val, f, null);
    }

    private void addTotal(PdfPTable t, String label, double val, Font f, BaseColor bg) {
        PdfPCell l = new PdfPCell(new Phrase(label, f));
        PdfPCell v = new PdfPCell(new Phrase(String.format("%.0f F", val), f));
        l.setBorder(Rectangle.NO_BORDER);
        v.setBorder(Rectangle.NO_BORDER);
        v.setHorizontalAlignment(Element.ALIGN_RIGHT);
        if (bg != null) {
            l.setBackgroundColor(bg);
            v.setBackgroundColor(bg);
        }
        t.addCell(l);
        t.addCell(v);
    }

    private long getNumeroLocal(Commande c) {
        return c.getPressing().getCommandes().stream().filter(x -> x.getId() <= c.getId()).count();
    }

    private String formatNumeroFacture(Long id) {
        return String.format("%09d", id);
    }
}
