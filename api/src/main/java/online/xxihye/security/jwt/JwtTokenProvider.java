package online.xxihye.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessExpiration;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret,
                            @Value("${jwt.access-expiration}") long expirationSec) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpiration = expirationSec * 1000;
    }

    public String createAccessToken(Long userNo) {
        Date now = new Date();
        Date expiredDate = new Date(now.getTime() + accessExpiration);

        return Jwts.builder()
                   .setSubject(String.valueOf(userNo))
                   .setIssuedAt(now)
                   .setExpiration(expiredDate)
                   .signWith(secretKey, SignatureAlgorithm.HS256)
                   .compact();
    }

    public Authentication getAuthentication(String token) {
        Long userNo = getUserNo(token);
        return new UsernamePasswordAuthenticationToken(
            userNo,
            null,
            Collections.emptyList()
        );
    }

    public Long getUserNo(String token) {
        Claims claims = parseClaims(token);
        return Long.parseLong(claims.getSubject());
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                   .setSigningKey(secretKey)
                   .build()
                   .parseClaimsJws(token)
                   .getBody();
    }
}
