package likelion.itgoserver.global.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum GlobalErrorCode implements ErrorCode {

    /**
     * 200 : 요청 성공
     */
    SUCCESS(HttpStatus.OK, "SUCCESS", "요청에 성공했습니다."),
    CREATED(HttpStatus.CREATED, "CREATED", "요청에 성공했으며 리소스가 정상적으로 생성되었습니다."),
    ACCEPTED(HttpStatus.ACCEPTED, "ACCEPTED", "요청에 성공했으나 처리가 완료되지 않았습니다."),

    /**
     * 300 : 리다이렉션
     */
    SEE_OTHER(HttpStatus.SEE_OTHER, "REDIRECT", "다른 주소로 요청해주세요."),

    /**
     * 400 : 요청 실패
     */
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "REQUEST_001", "잘못된 요청입니다."),
    INVALID_ENUM(HttpStatus.BAD_REQUEST, "REQUEST_002", "Enum 타입으로 변경할 수 없습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "REQUEST_003", "자격 증명이 이루어지지 않았습니다."),
    INVALID_PERMISSION(HttpStatus.FORBIDDEN, "REQUEST_004", "접근 권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "REQUEST_005", "잘못된 접근입니다."),
    REQUEST_TIMEOUT(HttpStatus.REQUEST_TIMEOUT, "REQUEST_006", "만료된 접근입니다."),
    NOT_SUPPORTED_URI(HttpStatus.NOT_FOUND, "REQUEST_007", "지원하지 않는 URL입니다."),
    NOT_SUPPORTED_METHOD(HttpStatus.METHOD_NOT_ALLOWED, "REQUEST_008", "지원하지 않는 HTTP Method 요청입니다."),
    NOT_SUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "REQUEST_009", "잘못된 미디어 타입입니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "REQUEST_010", "유효하지 않은 토큰입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "REQUEST_011", "토큰이 일치하지 않습니다"),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "REQUEST_012", "토큰의 유효기간이 만료되었습니다."),

    /**
     * 500 : 응답 실패
     */
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "RESPONSE_001", "서버와의 연결에 실패했습니다."),
    BAD_GATEWAY(HttpStatus.BAD_GATEWAY, "RESPONSE_002", "다른 서버로부터 잘못된 응답이 수신되었습니다."),
    INSUFFICIENT_STORAGE(HttpStatus.INSUFFICIENT_STORAGE, "RESPONSE_003", "서버의 용량이 부족해 요청에 실패했습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;

    GlobalErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}