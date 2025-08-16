package likelion.itgoserver.domain.trade.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TradeStatus {
    MATCHED("나눔 매칭"),     // Claim 수락 직후
    COMPLETED("나눔 완료"),
    CANCELED("나눔 취소");

    private final String description;
}