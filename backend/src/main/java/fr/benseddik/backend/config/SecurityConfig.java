package fr.benseddik.backend.config;

import fr.benseddik.backend.security.RateLimitFilter;
import fr.benseddik.backend.security.jwt.JwtAccessDeniedHandler;
import fr.benseddik.backend.security.jwt.JwtAuthenticationEntryPoint;
import fr.benseddik.backend.security.jwt.JwtAuthenticationFilter;
import fr.benseddik.backend.security.oauth2.OAuth2FailureHandler;
import fr.benseddik.backend.security.oauth2.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Configuration Spring Security 6.4.
 * <p>
 * 🛡️ Sécurité :
 * - Stateless (JWT, pas de sessions HTTP)
 * - CSRF désactivé (API REST stateless)
 * - CORS configuré pour le frontend
 * - Argon2 pour le hashage des mots de passe
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitFilter rateLimitFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailureHandler oAuth2FailureHandler;
    private final UserDetailsService userDetailsService;
    private final SecurityProperties securityProperties;

    /**
     * Endpoints publics (sans authentification).
     */
    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/v1/auth/**",
            "/api/v1/public/**",
            "/api/v1/users/verify-email",
            "/api/v1/users/resend-verification",
            "/api/v1/users/forgot-password",
            "/api/v1/users/reset-password",
            "/api/v1/users/reset-password/validate",
            "/actuator/health",
            "/actuator/info",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/error"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationProvider authenticationProvider) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)

                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )

                // 🛡️ SÉCURITÉ NIVEAU BANCAIRE : Headers HTTP de sécurité
                .headers(headers -> headers
                        // Content Security Policy : Empêche XSS et injection de code
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives(
                                    "default-src 'self'; " +
                                    "script-src 'self'; " +
                                    "style-src 'self' 'unsafe-inline'; " +
                                    "img-src 'self' data: https:; " +
                                    "font-src 'self'; " +
                                    "connect-src 'self'; " +
                                    "frame-ancestors 'none'; " +
                                    "base-uri 'self'; " +
                                    "form-action 'self'"
                                )
                        )
                        // X-Frame-Options : Empêche le clickjacking
                        .frameOptions(frame -> frame.deny())

                        // HTTP Strict Transport Security : Force HTTPS (1 an)
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000)  // 1 an
                        )

                        // X-Content-Type-Options : Empêche le MIME sniffing
                        .contentTypeOptions(contentType -> contentType.disable())  // Activé par défaut

                        // X-XSS-Protection : Protection XSS legacy (moderne = CSP)
                        .xssProtection(xss -> xss
                                .headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)
                        )

                        // Referrer-Policy : Contrôle des informations de référence
                        .referrerPolicy(referrer -> referrer
                                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                        )

                        // Permissions-Policy : Contrôle des fonctionnalités navigateur
                        .permissionsPolicy(permissions -> permissions
                                .policy("geolocation=(), microphone=(), camera=(), payment=()")
                        )
                )

                .authorizeHttpRequests(auth -> auth
                        // Endpoints publics
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/vet/**").hasAnyRole("VET", "ADMIN")
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler(oAuth2FailureHandler)
                )
                .authenticationProvider(authenticationProvider)
                // 🛡️ ORDRE DES FILTRES (important !) :
                // 1. Rate Limiting (bloquer les abus AVANT tout traitement)
                // 2. JWT Authentication (valider l'identité)
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configuration CORS.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(securityProperties.cors().allowedOrigins());
        configuration.setAllowedMethods(securityProperties.cors().allowedMethods());
        configuration.setAllowedHeaders(securityProperties.cors().allowedHeaders());
        configuration.setAllowCredentials(securityProperties.cors().allowCredentials());
        configuration.setMaxAge(securityProperties.cors().maxAge());

        // Headers exposés au frontend
        configuration.addExposedHeader("Authorization");
        configuration.addExposedHeader("X-Total-Count");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * 🛡️ Encodeur de mots de passe Argon2 - Niveau bancaire.
     *
     * Argon2id est plus sécurisé que BCrypt et résistant aux attaques GPU/ASIC.
     *
     * Paramètres (OWASP 2024 recommandations) :
     * - Salt : 16 bytes (128 bits)
     * - Hash : 32 bytes (256 bits)
     * - Parallelism : 4 (utilise 4 cores CPU)
     * - Memory : 64 MB (65536 KB) - standard bancaire
     * - Iterations : 4 (compromis sécurité/performance)
     *
     * Temps de hash : ~250-400ms sur serveur moderne
     * (acceptable pour authentification, trop long pour attaque brute force)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new Argon2PasswordEncoder(
            16,        // saltLength (128 bits)
            32,        // hashLength (256 bits)
            4,         // parallelism (4 threads CPU)
            1 << 16,   // memory: 64 MB = 65536 KB (niveau bancaire)
            4          // iterations (OWASP 2024 minimum)
        );
    }

    /**
     * Provider d'authentification.
     */
    @Bean
    public AuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }


    /**
     * Manager d'authentification.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration
    ) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}