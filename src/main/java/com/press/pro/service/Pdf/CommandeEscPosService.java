//package com.press.pro.service.Pdf;
//
//import com.itextpdf.text.*;
//import com.itextpdf.text.pdf.*;
//import com.press.pro.Entity.*;
//import org.springframework.stereotype.Service;
//
//import java.io.ByteArrayOutputStream;
//import java.io.FileOutputStream;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.time.format.DateTimeFormatter;
//
//@Service
//public class CommandePdfThermiqueService {
//
//    private static final String PDF_BASE_FOLDER = "pdfCommandes/";
//
//    public byte[] genererCommandePdfThermique(Commande commande, Utilisateur user) {
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        // Largeur typique pour ticket thermique (58 mm ~ 165 pt)
//        Rectangle receiptSize = new Rectangle(165, 600);
//        Document document = new Document(receiptSize, 5, 5, 5, 5);
//
//        try {
//            PdfWriter.getInstance(document, out);
//            document.open();
//
//            // Polices lisibles pour thermique
//            Font fontBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
//            Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 9);
//            Font fontSmall = FontFactory.getFont(FontFactory.HELVETICA, 8);
//
//            Pressing pressing = commande.getPressing();
//            Client client = commande.getClient();
//
//            // ========================
//            // ENTETE SIMPLE NOIR ET BLANC
//            // ========================
//            Paragraph header = new Paragraph(pressing.getNom(), fontBold);
//            header.setAlignment(Element.ALIGN_CENTER);
//            document.add(header);
//
//            if (pressing.getAdresse() != null && !pressing.getAdresse().isEmpty())
//                document.add(new Paragraph(pressing.getAdresse(), fontSmall));
//            document.add(new Paragraph("☎ " + pressing.getTelephone(), fontSmall));
//            document.add(new Paragraph("✉ " + (pressing.getEmail() != null ? pressing.getEmail() : "-"), fontSmall));
//
//            addSeparatorLine(document);
//
//            // ========================
//            // NUMERO DE COMMANDE
//            // ========================
//            Long numeroLocal = getNumeroLocal(commande);
//            Paragraph recuNo = new Paragraph("BON DE COMMANDE N° " + formatNumeroFacture(numeroLocal), fontBold);
//            recuNo.setAlignment(Element.ALIGN_CENTER);
//            document.add(recuNo);
//
//            addSeparatorLine(document);
//
//            // ========================
//            // CLIENT ET DATES
//            // ========================
//            DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//            PdfPTable infoTable = new PdfPTable(2);
//            infoTable.setWidthPercentage(100);
//            infoTable.setWidths(new float[]{2f, 1.5f});
//
//            // Client
//            PdfPCell clientCell = new PdfPCell();
//            clientCell.setBorder(Rectangle.NO_BORDER);
//            clientCell.addElement(new Paragraph("Client:", fontBold));
//            clientCell.addElement(new Paragraph(client != null ? client.getNom() : "CLIENT DIVERS", fontNormal));
//            if (client != null && client.getAdresse() != null)
//                clientCell.addElement(new Paragraph(client.getAdresse(), fontSmall));
//            infoTable.addCell(clientCell);
//
//            // Dates
//            PdfPCell dateCell = new PdfPCell();
//            dateCell.setBorder(Rectangle.NO_BORDER);
//            dateCell.addElement(new Paragraph("Réception: " +
//                    (commande.getDateReception() != null ? commande.getDateReception().format(df) : "-"), fontNormal));
//            dateCell.addElement(new Paragraph("Livraison: " +
//                    (commande.getDateLivraison() != null ? commande.getDateLivraison().format(df) : "-"), fontNormal));
//            infoTable.addCell(dateCell);
//
//            document.add(infoTable);
//            addSeparatorLine(document);
//
//            // ========================
//            // LIGNES DE COMMANDE
//            // ========================
//            PdfPTable lignesTable = new PdfPTable(4);
//            lignesTable.setWidthPercentage(100);
//            lignesTable.setWidths(new float[]{3f, 0.7f, 1f, 1.2f});
//
//            addSimpleTableHeader(lignesTable, new String[]{"Article", "Qté", "P.U", "Montant"}, fontBold);
//
//            for (CommandeLigne ligne : commande.getLignes()) {
//                Parametre param = ligne.getParametre();
//                String article = param != null ? param.getArticle() : "-";
//                String ab = param != null ? abregerService(param.getService()) : "-";
//                double pu = param != null ? param.getPrix() : 0;
//                double montant = ligne.getQuantite() * pu;
//
//                lignesTable.addCell(createCellLeft(article + " (" + ab + ")", fontNormal));
//                lignesTable.addCell(createCellCenter(String.valueOf(ligne.getQuantite()), fontNormal));
//                lignesTable.addCell(createCellRight(String.format("%.0f F", pu), fontNormal));
//                lignesTable.addCell(createCellRight(String.format("%.0f F", montant), fontBold));
//            }
//
//            document.add(lignesTable);
//            addSeparatorLine(document);
//
//            // ========================
//            // TOTAUX
//            // ========================
//            double total = commande.getLignes().stream()
//                    .mapToDouble(l -> (l.getParametre() != null ? l.getParametre().getPrix() : 0) * l.getQuantite())
//                    .sum();
//            double remise = commande.getRemise();
//            double net = total - remise;
//            double paye = commande.getMontantPaye();
//            double reste = net - paye;
//
//            PdfPTable totauxTable = new PdfPTable(2);
//            totauxTable.setWidthPercentage(100);
//            totauxTable.setWidths(new float[]{2f, 1.2f});
//
//            addTotalRow(totauxTable, "Montant Total", total, fontNormal);
//            if (remise > 0) addTotalRow(totauxTable, "Remise", remise, fontNormal);
//            addTotalRow(totauxTable, "Net à Payer", net, fontBold);
//            if (paye > 0) addTotalRow(totauxTable, "Montant Payé", paye, fontNormal);
//            addTotalRow(totauxTable, "Reste à Payer", reste, fontBold);
//
//            document.add(totauxTable);
//
//            addSeparatorLine(document);
//
//            // ========================
//            // MESSAGE DE FIN
//            // ========================
//            Paragraph userInfo = new Paragraph("Émis par : " + user.getEmail(), fontSmall);
//            userInfo.setAlignment(Element.ALIGN_CENTER);
//            document.add(userInfo);
//
//            Paragraph merci = new Paragraph("Merci pour votre confiance!", fontBold);
//            merci.setAlignment(Element.ALIGN_CENTER);
//            document.add(merci);
//
//            document.close();
//
//            // SAVE PDF
//            String folder = PDF_BASE_FOLDER + pressing.getNom().replaceAll("[^a-zA-Z0-9]", "_") + "/";
//            Files.createDirectories(Paths.get(folder));
//            try (FileOutputStream fos = new FileOutputStream(folder + "commande_" + commande.getId() + ".pdf")) {
//                fos.write(out.toByteArray());
//            }
//
//        } catch (Exception e) {
//            throw new RuntimeException("Erreur PDF thermique : " + e.getMessage(), e);
//        }
//
//        return out.toByteArray();
//    }
//
//    // ========================
//    // MÉTHODES UTILES
//    // ========================
//
//    private void addSeparatorLine(Document doc) throws DocumentException {
//        LineSeparator line = new LineSeparator();
//        line.setLineWidth(1f);
//        doc.add(new Chunk(line));
//    }
//
//    private void addSimpleTableHeader(PdfPTable table, String[] headers, Font font) {
//        for (String h : headers) {
//            PdfPCell c = new PdfPCell(new Phrase(h, font));
//            c.setHorizontalAlignment(Element.ALIGN_CENTER);
//            c.setBorder(Rectangle.BOX);
//            table.addCell(c);
//        }
//    }
//
//    private void addTotalRow(PdfPTable table, String label, double amount, Font font) {
//        table.addCell(createCellLeft(label, font));
//        table.addCell(createCellRight(String.format("%.0f F", amount), font));
//    }
//
//    private String abregerService(String s) {
//        if (s == null || s.isBlank()) return "-";
//        s = s.toUpperCase().replace("+", " ");
//        String[] mots = s.split("\\s+");
//        StringBuilder a = new StringBuilder();
//        for (String m : mots) {
//            if (!m.isBlank()) a.append(m.charAt(0));
//            if (a.length() == 3) break;
//        }
//        return a.toString();
//    }
//
//    private long getNumeroLocal(Commande c) {
//        Pressing p = c.getPressing();
//        return p.getCommandes().stream().filter(x -> x.getId() <= c.getId()).count();
//    }
//
//    private String formatNumeroFacture(Long id) {
//        return String.format("%09d", id == null ? 0L : id);
//    }
//
//    private PdfPCell createCellLeft(String text, Font font) {
//        PdfPCell c = new PdfPCell(new Phrase(text, font));
//        c.setBorder(Rectangle.NO_BORDER);
//        c.setHorizontalAlignment(Element.ALIGN_LEFT);
//        return c;
//    }
//
//    private PdfPCell createCellCenter(String text, Font font) {
//        PdfPCell c = new PdfPCell(new Phrase(text, font));
//        c.setBorder(Rectangle.NO_BORDER);
//        c.setHorizontalAlignment(Element.ALIGN_CENTER);
//        return c;
//    }
//
//    private PdfPCell createCellRight(String text, Font font) {
//        PdfPCell c = new PdfPCell(new Phrase(text, font));
//        c.setBorder(Rectangle.NO_BORDER);
//        c.setHorizontalAlignment(Element.ALIGN_RIGHT);
//        return c;
//    }
//}
