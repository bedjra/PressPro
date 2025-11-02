//package com.eduMap.edumap.GLOBALE.config;
//
//import jakarta.annotation.PostConstruct;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import javax.crypto.SecretKey;
//import java.util.Date;
//import java.util.logging.Logger;
//
//@Component
//public class JwtUtil {
//
//    private static final Logger LOGGER = Logger.getLogger(JwtUtil.class.getName());
//
//    @Value("${jwt.secret}")
//    private String secret;
//
//    private SecretKey getSigningKey() {
//        byte[] keyBytes = Decoders.BASE64.decode(secret);
//        return Keys.hmacShaKeyFor(keyBytes);
//    }
//
//    public String generateToken(String email, String role) {
//        long expirationTime = 86400000; // 24h
//        try {
//            String token = Jwts.builder()
//                    .setSubject(email)
//                    .claim("role", role)
//                    .setIssuedAt(new Date())
//                    .setExpiration(new Date(Systeme.currentTimeMillis() + expirationTime))
//                    .signWith(getSigningKey(), SignatureAlgorithm.HS512)
//                    .compact();
//            Systeme.out.println("✅ Token généré : " + token);
//            return token;
//        } catch (Exception e) {
//            Systeme.err.println("❌ Erreur lors de la génération du token : " + e.getMessage());
//            throw e;
//        }
//    }
//
//    public String extractEmail(String token) {
//        return getClaims(token).getSubject();
//    }
//
//    public String extractRole(String token) {
//        return getClaims(token).get("role", String.class);
//    }
//
//    public boolean validateToken(String token) {
//        try {
//            getClaims(token);
//            return true;
//        } catch (JwtException e) {
//            LOGGER.warning("❌ Token invalide : " + e.getMessage());
//            return false;
//        }
//    }
//
//    private Claims getClaims(String token) {
//        return Jwts.parserBuilder()
//                .setSigningKey(getSigningKey())
//                .build()
//                .parseClaimsJws(token)
//                .getBody();
//    }
//
//    @PostConstruct
//    public void validateKey() {
//        try {
//            SecretKey key = getSigningKey();
//            int size = key.getEncoded().length;
//            LOGGER.info("✅ Clé JWT chargée avec succès. Taille : " + size + " octets.");
//            if (size < 64) {
//                LOGGER.warning("⚠️ ATTENTION : La clé est trop courte pour HS512 (besoin de ≥ 64 octets).");
//            }
//        } catch (Exception e) {
//            LOGGER.severe("❌ Erreur de chargement de la clé JWT : " + e.getMessage());
//        }
//    }
//}
