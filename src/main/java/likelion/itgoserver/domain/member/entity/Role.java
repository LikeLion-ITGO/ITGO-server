package likelion.itgoserver.domain.member.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    ROLE_ADMIN("ROLE_ADMIN", "관리자"),
    ROLE_MEMBER("ROLE_MEMBER", "일반회원")
    ;

    private final String key;
    private final String description;
}