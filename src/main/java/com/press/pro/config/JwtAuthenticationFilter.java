package com.press.pro.config;

import com.press.pro.repository.UtilisateurRepository;
import com.press.pro.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String email;

        // Si pas de header Authorization → continuer sans auth
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7); // on enlève "Bearer "
        email = jwtService.extractEmail(jwt); // ✅ on utilise ton extractEmail()

        // Si l'utilisateur n'est pas encore authentifié
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            var utilisateur = utilisateurRepository.findByEmail(email).orElse(null);

            if (utilisateur != null) {
                // Vérifie simplement que le token n’est pas expiré
                // (tu peux plus tard ajouter une méthode jwtService.isTokenValid)
                if (!isTokenExpired(jwt)) {
                    var authToken = new UsernamePasswordAuthenticationToken(
                            utilisateur, null, utilisateur.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    

    private boolean isTokenExpired(String token) {
        try {
            var claims = io.jsonwebtoken.Jwts.parser()
                    .setSigningKey(jwtService.getSecretKey())
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getExpiration().before(new java.util.Date());
        } catch (Exception e) {
            return true;
        }
    }
}
