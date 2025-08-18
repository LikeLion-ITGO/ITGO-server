package likelion.itgoserver.domain.store.service;

import likelion.itgoserver.domain.member.entity.Member;
import likelion.itgoserver.domain.member.repository.MemberRepository;
import likelion.itgoserver.domain.store.dto.StoreImageDtos.StoreImageConfirmRequest;
import likelion.itgoserver.domain.store.entity.Store;
import likelion.itgoserver.domain.store.repository.StoreRepository;
import likelion.itgoserver.global.error.GlobalErrorCode;
import likelion.itgoserver.global.error.exception.CustomException;
import likelion.itgoserver.global.infra.s3.service.S3ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class StoreImageService {

    private final MemberRepository memberRepository;
    private final StoreRepository storeRepository;
    private final S3ImageService s3ImageService;

    public void confirmStoreImage(Long memberId, StoreImageConfirmRequest req) {
        validateStoreOwner(memberId, req.storeId());

        // S3 Key 검증
        String key = req.objectKey();
        if (key == null || key.isBlank()) {
            throw new CustomException(GlobalErrorCode.BAD_REQUEST, "objectKey가 비어 있습니다.");
        }
        assertKeyBelongsToStore(req.storeId(), key);

        // 실제 업로드 검증
        if (!s3ImageService.doesObjectExist(req.objectKey())) {
            throw new CustomException(GlobalErrorCode.BAD_REQUEST, "업로드되지 않은 객체입니다.");
        }

        // S3 Key 업데이트
        Store store = storeRepository.findById(req.storeId())
                .orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND, "가게가 존재하지 않습니다."));
        String oldKey = store.getStoreImageKey();
        store.updateImageKey(req.objectKey());

        // 이전 키 삭제
        if (oldKey != null && !oldKey.equals(key)) {
            try {
                s3ImageService.deleteObject(oldKey);
            } catch (Exception e) {
                log.warn("Failed to delete old image: {}", oldKey, e);
            }
        }
    }

    @Transactional(readOnly = true)
    public void validateStoreOwner(Long memberId, Long storeId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND, "회원이 존재하지 않습니다."));
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND, "가게가 존재하지 않습니다."));

        if (store.getOwner() == null || !store.getOwner().getId().equals(member.getId())) {
            throw new CustomException(GlobalErrorCode.INVALID_PERMISSION, "본인 가게가 아닙니다.");
        }
    }

    private void assertKeyBelongsToStore(Long storeId, String key) {
        String expectedPrefix = "stores/%d/cover/".formatted(storeId);
        if (!key.startsWith(expectedPrefix)) {
            throw new CustomException(GlobalErrorCode.BAD_REQUEST, "해당 가게의 키가 아닙니다.");
        }
    }
}
