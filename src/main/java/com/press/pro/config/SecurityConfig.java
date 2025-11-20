package com.press.pro.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 🚫 Désactive CSRF (utile uniquement pour les applis web avec session)
                .csrf(csrf -> csrf.disable())

                // ✅ Active CORS avec la configuration personnalisée
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // ✅ Configuration des permissions
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/public/save").permitAll()
                        .requestMatchers(
                                "/api/auth/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/api-docs/**",
                                "/webjars/**",
                                "/error"
                        ).permitAll()

                        // Le reste nécessite un JWT valide
                        .anyRequest().authenticated()
                )

                // ✅ Pas de session : JWT only
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // ✅ Ajoute le filtre JWT avant le filtre d'auth standard
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * ✅ Configuration CORS globale
     * Permet à ton frontend (localhost:8080) de communiquer avec le backend
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 🌍 Origines autorisées (ton frontend)
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:8080",   // Ton frontend actuel
                "http://localhost:5173",   // Vite par défaut
                "http://localhost:3000",
                "https://press-pro.vercel.app"  // Frontend déployé
        ));

        // 📡 Méthodes HTTP autorisées
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));

        // 📋 Headers autorisés dans les requêtes
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "X-Requested-With",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));

        // 📤 Headers exposés au frontend (visible dans les réponses)
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Disposition"
        ));

        // 🔐 Autoriser les credentials (cookies, Authorization header)
        configuration.setAllowCredentials(true);

        // ⏱️ Cache de la configuration CORS (1 heure)
        configuration.setMaxAge(3600L);

        // Appliquer cette config à tous les endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    // ✅ Gestionnaire d'authentification
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    // ✅ Encodeur de mots de passe
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}