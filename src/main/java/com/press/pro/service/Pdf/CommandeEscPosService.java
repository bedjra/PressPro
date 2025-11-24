//package com.press.pro.service.Pdf;
//
//
//import com.press.pro.Entity.Commande;
//import com.press.pro.Entity.Pressing;
//import com.press.pro.Entity.Client;
//import com.press.pro.Entity.Parametre;
//
//
//import java.awt.image.BufferedImage;
//import javax.imageio.ImageIO;
//import java.io.ByteArrayInputStream;
//
//public class CommandeEscPosService {
//
//    public void imprimerCommande(Commande commande, String port) {
//        try (PrinterOutputStream printer = new PrinterOutputStream(port)) { // ex: "LPT1" ou "/dev/usb/lp0"
//            EscPos escpos = new EscPos(printer);
//
//            Pressing pressing = commande.getPressing();
//            Client client = commande.getClient();
//            Parametre param = commande.getParametre();
//
//            // --- Logo ---
//            if (pressing.getLogo() != null && pressing.getLogo().length > 0) {
//                BufferedImage logo = ImageIO.read(new ByteArrayInputStream(pressing.getLogo()));
//                escpos.writeImage(new BitImageWrapper(), logo);
//            }
//
//            // --- Info Pressing ---
//            Style styleTitre = new Style().setBold(true).setFontSize(Style.FontSize._2, Style.FontSize._2);
//            escpos.writeLF(pressing.getNom(), styleTitre);
//            escpos.writeLF(pressing.getAdresse());
//            escpos.writeLF("Tél: " + pressing.getTelephone());
//            if (pressing.getCel() != null && !pressing.getCel().isBlank())
//                escpos.writeLF("Cel: " + pressing.getCel());
//            if (pressing.getEmail() != null)
//                escpos.writeLF(pressing.getEmail());
//
//            escpos.feed(1);
//
//            // --- Reçu ---
//            escpos.writeLF("Reçu N° " + String.format("%09d", commande.getId()), styleTitre);
//            escpos.writeLF("--------------------------------");
//
//            // --- Client ---
//            escpos.writeLF("Client : " + (client != null ? client.getNom() : "CLIENT DIVERS"));
//            escpos.writeLF("Adresse: " + (client != null && client.getAdresse() != null ? client.getAdresse() : "-"));
//
//            escpos.writeLF("--------------------------------");
//
//            // --- Commande ---
//            int qte = commande.getQte();
//            double prix = param != null ? param.getPrix() : 0.0;
//            double montant = prix * qte;
//            double remise = commande.getRemise();
//            double net = montant - remise;
//            double paye = commande.getMontantPaye();
//            double reste = net - paye;
//
//            escpos.writeLF(String.format("%-10s %3s %6s %6s", "Article", "Qté", "Prix", "Mont"));
//            escpos.writeLF(String.format("%-10s %3d %6.0f %6.0f",
//                    param != null ? param.getArticle() : "-",
//                    qte,
//                    prix,
//                    montant));
//            escpos.writeLF("--------------------------------");
//
//            escpos.writeLF(String.format("%-12s %10.0f", "Montant HT:", net));
//            escpos.writeLF(String.format("%-12s %10.0f", "Montant payé:", paye));
//            escpos.writeLF(String.format("%-12s %10.0f", "Reste à payer:", reste));
//            escpos.writeLF("--------------------------------");
//
//            // --- Signature ---
//            escpos.writeLF("Signature:", new Style().setUnderline(true));
//
//            escpos.feed(5).cut(EscPos.CutMode.FULL);
//            escpos.close();
//
//        } catch (Exception e) {
//            throw new RuntimeException("Erreur impression ESC/POS : " + e.getMessage(), e);
//        }
//    }
//}
