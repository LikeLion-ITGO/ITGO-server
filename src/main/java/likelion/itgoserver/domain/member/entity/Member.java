package likelion.itgoserver.domain.member.entity;

import jakarta.persistence.*;
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

}