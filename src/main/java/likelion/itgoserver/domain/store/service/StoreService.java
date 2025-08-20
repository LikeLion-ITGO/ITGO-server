package likelion.itgoserver.domain.store.service;

import likelion.itgoserver.domain.member.entity.Member;
import likelion.itgoserver.domain.member.repository.MemberRepository;
import likelion.itgoserver.domain.store.dto.StoreInfoResponse;
import likelion.itgoserver.domain.store.dto.StoreRegisterRequest;
import likelion.itgoserver.domain.store.dto.StoreUpdateRequest;
import likelion.itgoserver.domain.store.entity.Store;
import likelion.itgoserver.domain.store.repository.StoreRepository;
import likelion.itgoserver.global.error.GlobalErrorCode;
import likelion.itgoserver.global.error.exception.CustomException;
import likelion.itgoserver.global.infra.s3.service.PublicUrlResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final MemberRepository memberRepository;
    private final PublicUrlResolver publicUrlResolver;

    /**
     * 가게 정보 등록
     */
    @Transactional
    public StoreInfoResponse registerStore(Long memberId, StoreRegisterRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND, "해당 ID의 회원을 찾을 수 없습니다: " + memberId));

        // 연관관계 설정 + DB 추가
        Store store = request.toEntity();
        member.registerStore(store);
        storeRepository.save(store);

        String imageUrl = publicUrlResolver.toUrl(store.getStoreImageKey());
        return StoreInfoResponse.of(store, imageUrl);
    }

    /**
     * 가게 정보 조회
     */
    public StoreInfoResponse getStore(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND, "해당 ID의 가게를 찾을 수 없습니다: " + storeId));

        String imageUrl = publicUrlResolver.toUrl(store.getStoreImageKey());
        return StoreInfoResponse.of(store, imageUrl);
    }

    /**
     * 사용자 본인 가게 정보 조회
     */
    public StoreInfoResponse getStoreByMemberId(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND, "해당 ID의 회원을 찾을 수 없습니다: " + memberId));

        Store store = member.getStore();
        if (store == null) {
            throw new CustomException(GlobalErrorCode.NOT_FOUND, "해당 회원은 등록된 가게가 없습니다.");
        }

        String imageUrl = publicUrlResolver.toUrl(store.getStoreImageKey());
        return StoreInfoResponse.of(store, imageUrl);
    }

    /**
     * 가게 정보 수정
     */
    @Transactional
    public StoreInfoResponse updateStore(Long memberId, StoreUpdateRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND, "해당 ID의 회원을 찾을 수 없습니다: " + memberId));

        Store store = member.getStore();
        if (store == null) {
            throw new CustomException(GlobalErrorCode.NOT_FOUND, "등록된 가게가 없습니다.");
        }

        // Store 엔티티에서 직접 업데이트
        store.update(request);
        String imageUrl = publicUrlResolver.toUrl(store.getStoreImageKey());
        return StoreInfoResponse.of(store, imageUrl);
    }

    /**
     * 가게 정보 삭제
     */
    @Transactional
    public void deleteStore(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.NOT_FOUND, "해당 ID의 회원을 찾을 수 없습니다: " + memberId));

        Store store = member.getStore();
        if (store == null) {
            throw new CustomException(GlobalErrorCode.NOT_FOUND, "삭제할 가게가 존재하지 않습니다.");
        }

        // 연관관계 끊기 + DB 삭제
        member.removeStore();
        storeRepository.delete(store);
    }

}