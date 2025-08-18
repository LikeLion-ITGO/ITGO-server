package likelion.itgoserver.domain.claim.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ClaimStatus {
    PENDING("대기중"),
    ACCEPTED("수락됨"),
    REJECTED("거절됨"),
    CANCELED("취소됨"),
    ;

    private final String description;
}