package com.press.pro.service;

import com.press.pro.Entity.Utilisateur;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    private final String SECRET_KEY = "p5U7ZVkQ0ivBBEz0XJgnDvVCmj8vLTrTHVcVhu0xz0g=";

    public String generateToken(Utilisateur user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole());
        claims.put("pressing_id", user.getPressing() != null ? user.getPressing().getId() : null);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 24h
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public String extractEmail(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String extractUsername(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
    public String getSecretKey() {
        return SECRET_KEY;
    }



}
