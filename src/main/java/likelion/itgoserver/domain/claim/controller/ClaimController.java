package likelion.itgoserver.domain.claim.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import likelion.itgoserver.domain.claim.dto.ClaimCreateRequest;
import likelion.itgoserver.domain.claim.dto.ClaimResponse;
import likelion.itgoserver.domain.claim.dto.ReceivedClaimItem;
import likelion.itgoserver.domain.claim.dto.SentClaimItem;
import likelion.itgoserver.domain.claim.service.ClaimService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Claim API", description = "거래 신청(Claim) 생성/처리/조회")
@RestController
@RequestMapping("/api/v1/claim")
@RequiredArgsConstructor
@Validated
public class ClaimController {

    private final ClaimService claimService;

    @Operation(summary = "거래 신청 생성")
    @PostMapping()
    public ResponseEntity<ClaimResponse> create(@Valid @RequestBody ClaimCreateRequest request) {
        var resp = claimService.request(request.wishId(), request.shareId());
        return ResponseEntity.ok(resp);
    }

    @Operation(summary = "거래 신청 취소")
    @PostMapping("/cancel/{claimId}")
    public ResponseEntity<ClaimResponse> cancel(@PathVariable Long claimId) {
        return ResponseEntity.ok(claimService.cancel(claimId));
    }

    @Operation(summary = "거래 신청 수락")
    @PostMapping("/accept/{claimId}")
    public ResponseEntity<ClaimResponse> accept(@PathVariable Long claimId) {
        return ResponseEntity.ok(claimService.accept(claimId));
    }

    @Operation(summary = "거래 신청 거절")
    @PostMapping("/reject/{claimId}")
    public ResponseEntity<ClaimResponse> reject(@PathVariable Long claimId) {
        return ResponseEntity.ok(claimService.reject(claimId));
    }

    @Operation(summary = "특정 Wish의 보낸 요청 목록")
    @GetMapping("/sent/{wishId}")
    public ResponseEntity<Page<SentClaimItem>> listByWish(
            @PathVariable Long wishId,
            @ParameterObject @PageableDefault(size = 20, sort = "regDate", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(claimService.sentByWish(wishId, pageable));
    }

    @Operation(summary = "특정 Share의 받은 요청 목록")
    @GetMapping("/received/{shareId}")
    public ResponseEntity<Page<ReceivedClaimItem>> listByShare(
            @PathVariable Long shareId,
            @ParameterObject @PageableDefault(size = 20, sort = "regDate", direction = Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(claimService.receivedByShare(shareId, pageable));
    }

}