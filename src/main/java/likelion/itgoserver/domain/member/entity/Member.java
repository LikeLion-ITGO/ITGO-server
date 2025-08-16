package likelion.itgoserver.domain.member.entity;

import jakarta.persistence.*;
import likelion.itgoserver.domain.store.entity.Store;
import likelion.itgoserver.global.error.GlobalErrorCode;
import likelion.itgoserver.global.error.exception.CustomException;
import likelion.itgoserver.global.support.BaseTimeEntity;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    /**
     * 회원가입 시 등록되는 정보
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, length = 20, unique = true)
    private String username;

    @Enumerated(value = EnumType.STRING)
    private Role role;

    /**
     * 마이페이지에서 등록하는 정보
     */
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "store_id")
    private Store store;

    /**
     * 연관관계 메서드
     */
    public void registerStore(Store store) {
        if (this.store != null) {
            throw new CustomException(GlobalErrorCode.BAD_REQUEST, "해당 회원은 이미 가게를 등록했습니다.");
        }
        this.store = store;
        store.assignOwner(this);
    }

    public void removeStore() {
        this.store = null;
    }
}