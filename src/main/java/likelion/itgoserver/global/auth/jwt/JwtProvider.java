package likelion.itgoserver.global.auth.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import likelion.itgoserver.domain.member.entity.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
public class JwtProvider {

    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";

    private final SecretKey secretKey;
    private final long accessTokenValidityInMilliseconds;
    private final long refreshTokenValidityInMilliseconds;

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-validity-in-seconds}") long accessTokenValidityInSeconds,
            @Value("${jwt.refresh-token-validity-in-seconds}") long refreshTokenValidityInSeconds) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenValidityInMilliseconds = accessTokenValidityInSeconds * 1000;
        this.refreshTokenValidityInMilliseconds = refreshTokenValidityInSeconds * 1000;
    }

    /**
     * 토큰 생성
     */
    public String createAccessToken(Long userId, Long memberId, Role role) {
        return createToken(userId, accessTokenValidityInMilliseconds, TOKEN_TYPE_ACCESS, memberId, role);
    }

    public String createRefreshToken(Long userId) {
        return createToken(userId, refreshTokenValidityInMilliseconds, TOKEN_TYPE_REFRESH, null, null);
    }

    private String createToken(Long userId, long validityInMillis, String tokenType, Long memberId, Role role) {
        Date now = new Date();
        JwtBuilder builder = Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("tokenType", tokenType)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + validityInMillis))
                .signWith(secretKey, SignatureAlgorithm.HS512);

        if (memberId != null) builder.claim("memberId", memberId);
        if (role != null) builder.claim("role", role.getKey());

        return builder.compact();
    }

    /**
     * 클레임 추출
     */
    public Long getUserId(String token) {
        return Long.parseLong(getClaims(token).getSubject());
    }

    public Long getMemberId(String token) {
        return getClaims(token).get("memberId", Long.class);
    }

    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    public Date getExpiration(String token) {
        return getClaims(token).getExpiration();
    }

    /**
     * 토큰 유효성 및 상태 검증
     */
    public boolean isValidAccessToken(String token) {
        return validateToken(token) && !isTokenExpired(token) && isAccessToken(token);
    }

    public boolean isValidRefreshToken(String token) {
        return validateToken(token) && !isTokenExpired(token) && isRefreshToken(token);
    }

    private boolean isRefreshToken(String token) {
        return TOKEN_TYPE_REFRESH.equals(getTokenType(token));
    }

    private boolean isAccessToken(String token) {
        return TOKEN_TYPE_ACCESS.equals(getTokenType(token));
    }

    private String getTokenType(String token) {
        try {
            return getClaims(token).get("tokenType", String.class);
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("토큰 타입 추출 실패: {}", e.getMessage());
            return null;
        }
    }

    private boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("토큰 유효성 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        try {
            return getExpiration(token).before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("만료 확인 중 예외 발생: {}", e.getMessage());
            return true;
        }
    }

    /**
     * 내부 유틸
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
    }

}