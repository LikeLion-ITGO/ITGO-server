package likelion.itgoserver.domain.wish.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Wish 생성 + 매칭 응답")
public record WishCreateAndMatchResponse(
        Long wishId,
        List<WishMatchItem> matches
) {}