package likelion.itgoserver.global.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenValidityInSeconds;

    private static final String REFRESH_TOKEN_KEY_PREFIX = "refresh_token:";
    private static final String TOKEN_BLACKLIST_KEY_PREFIX = "blacklist:";

    private final RedisTemplate<String, String> redisTemplate;

    private String getRefreshTokenKey(Long userId) {
        return REFRESH_TOKEN_KEY_PREFIX + userId;
    }

    private String getBlacklistKey(String token) {
        return TOKEN_BLACKLIST_KEY_PREFIX + Math.abs(token.hashCode());
    }

    /**
     * Refresh Token 저장 (기존 토큰 무효화 포함)
     */
    public void save(Long userId, String token) {
        // 1. 기존 토큰이 있다면 블랙리스트에 추가 (보안)
        invalidateExistingToken(userId);

        // 2. Redis 저장 (TTL 자동 만료 처리)
        String key = getRefreshTokenKey(userId);
        redisTemplate.opsForValue().set(key, token, Duration.ofSeconds(refreshTokenValidityInSeconds));
    }

    /**
     * 토큰 조회
     */
    public Optional<String> getToken(Long userId) {
        String key = getRefreshTokenKey(userId);
        return Optional.ofNullable(redisTemplate.opsForValue().get(key));
    }

    /**
     * 토큰 삭제
     */
    public void delete(Long userId) {
        // 1. 기존 토큰을 블랙리스트에 추가 (보안)
        invalidateExistingToken(userId);

        // 2. Redis에서 삭제
        String key = getRefreshTokenKey(userId);
        redisTemplate.delete(key);
    }

    /**
     * 토큰 유효성 검증
     */
    public boolean validateToken(Long userId, String token) {
        // 1. 블랙리스트 체크
        if (isTokenBlacklisted(token)) {
            log.warn("블랙리스트에 등록된 토큰 사용 시도 - userId: {}", userId);
            return false;
        }

        // 2. Redis에서 토큰 조회 및 비교
        return getToken(userId)
                .map(storedToken -> storedToken.equals(token))
                .orElse(false);
    }

    /**
     * 내부 유틸
     */
    private void invalidateExistingToken(Long userId) {
        getToken(userId).ifPresent(this::addToBlacklist);
    }

    private boolean isTokenBlacklisted(String token) {
        String blacklistKey = getBlacklistKey(token);
        return Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey));
    }

    private void addToBlacklist(String token) {
        String blacklistKey = getBlacklistKey(token);
        // 토큰 만료 시간만큼 블랙리스트에 보관 (TTL로 자동 정리)
        redisTemplate.opsForValue().set(
                blacklistKey,
                "revoked",
                Duration.ofSeconds(refreshTokenValidityInSeconds)
        );
        log.debug("토큰이 블랙리스트에 추가됨: {}", blacklistKey);
    }

}