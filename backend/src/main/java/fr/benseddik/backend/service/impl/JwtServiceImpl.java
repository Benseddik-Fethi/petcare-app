package fr.benseddik.backend.service.impl;

import fr.benseddik.backend.config.JwtProperties;
import fr.benseddik.backend.domain.User;
import fr.benseddik.backend.service.JwtService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

/**
 * Implémentation du service JWT avec JJWT 0.13.0.
 *
 * 🛡️ Sécurité :
 * - Clé HMAC-SHA256 (256 bits minimum)
 * - Claims standards (iss, aud, sub, exp, iat)
 * - Distinction access/refresh via claim "type"
 * - Hash SHA-256 pour stockage des refresh tokens
 */
@Service
@Slf4j
public class JwtServiceImpl implements JwtService {

    private static final String CLAIM_USER_ID = "uid";
    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_TYPE = "type";
    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";

    private final SecretKey secretKey;
    private final JwtProperties jwtProperties;
    private final JwtParser jwtParser;

    public JwtServiceImpl(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
        this.jwtParser = Jwts.parser()
                .verifyWith(secretKey)
                .requireIssuer(jwtProperties.issuer())
                .requireAudience(jwtProperties.audience())
                .build();
    }

    @Override
    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiration = now.plus(jwtProperties.accessToken().expiration());

        return Jwts.builder()
                .issuer(jwtProperties.issuer())
                .audience().add(jwtProperties.audience()).and()
                .subject(user.getEmail())
                .claim(CLAIM_USER_ID, user.getId().toString())
                .claim(CLAIM_EMAIL, user.getEmail())
                .claim(CLAIM_ROLE, user.getRole().name())
                .claim(CLAIM_TYPE, TOKEN_TYPE_ACCESS)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(secretKey)
                .compact();
    }

    @Override
    public String generateRefreshToken(User user) {
        Instant now = Instant.now();
        Instant expiration = now.plus(jwtProperties.refreshToken().expiration());

        return Jwts.builder()
                .issuer(jwtProperties.issuer())
                .audience().add(jwtProperties.audience()).and()
                .subject(user.getEmail())
                .claim(CLAIM_USER_ID, user.getId().toString())
                .claim(CLAIM_TYPE, TOKEN_TYPE_REFRESH)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(secretKey)
                .compact();
    }

    @Override
    public boolean isTokenValid(String token) {
        try {
            jwtParser.parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("Token expiré: {}", e.getMessage());
        } catch (SecurityException e) {
            log.warn("Signature JWT invalide: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("Token JWT malformé: {}", e.getMessage());
        } catch (JwtException e) {
            log.warn("Token JWT invalide: {}", e.getMessage());
        }
        return false;
    }

    @Override
    public Optional<UUID> extractUserId(String token) {
        return extractClaim(token, CLAIM_USER_ID)
                .map(UUID::fromString);
    }

    @Override
    public Optional<String> extractEmail(String token) {
        return extractClaim(token, CLAIM_EMAIL);
    }

    @Override
    public boolean isAccessToken(String token) {
        return extractClaim(token, CLAIM_TYPE)
                .map(TOKEN_TYPE_ACCESS::equals)
                .orElse(false);
    }

    @Override
    public String hashToken(String refreshToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(refreshToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 est toujours disponible en Java
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * Extrait un claim du token.
     */
    private Optional<String> extractClaim(String token, String claimName) {
        try {
            Claims claims = jwtParser.parseSignedClaims(token).getPayload();
            return Optional.ofNullable(claims.get(claimName, String.class));
        } catch (JwtException e) {
            log.debug("Impossible d'extraire le claim {}: {}", claimName, e.getMessage());
            return Optional.empty();
        }
    }
}