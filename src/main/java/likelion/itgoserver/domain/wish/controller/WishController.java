package likelion.itgoserver.domain.wish.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import likelion.itgoserver.domain.wish.dto.WishCreateAndMatchResponse;
import likelion.itgoserver.domain.wish.dto.WishUpsertRequest;
import likelion.itgoserver.domain.wish.service.WishService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Wish API", description = "필요한 재고 요청 생성 및 즉시 매칭")
@RestController
@RequestMapping("/api/v1/wish")
@RequiredArgsConstructor
@Validated
public class WishController {

    private final WishService wishService;

    @Operation(
            summary = "Wish 생성 + 즉시 매칭",
            description = """
            사용자가 Wish를 저장하면, 같은 트랜잭션 흐름에서 조건을 만족하는 Share 리스트를 같이 반환
            기본 매칭 조건:
            1) wish.store.address.dong == share.store.address.dong
            2) wish.itemName == share.itemName
            3) share.quantity >= wish.quantity
            4) 거래 가능 시간대가 겹침 (open/close overlap)
            + 유통기한 및 km 반경 선택 옵션
            """
    )
    @PostMapping("/match")
    public ResponseEntity<WishCreateAndMatchResponse> createAndMatch(
            @Valid @RequestBody WishUpsertRequest request,
            @RequestParam(defaultValue = "3") double radiusKm,     // 선택: 근처 반경(없으면 동 기준만)
            @RequestParam(defaultValue = "10") int size            // 한 번에 보여줄 개수
    ) {
        var resp = wishService.createAndMatch(request, radiusKm, PageRequest.of(0, size));
        return ResponseEntity.ok(resp);
    }
}