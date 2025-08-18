package likelion.itgoserver.domain.share.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StorageType {
    REFRIGERATED("냉장"),
    FROZEN("냉동"),
    ROOM_TEMPERATURE("상온")
    ;

    private final String description;
}