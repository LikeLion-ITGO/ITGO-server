package likelion.itgoserver.domain.user.entity;

import jakarta.persistence.*;
import likelion.itgoserver.domain.member.entity.Member;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login_id", nullable = false, length = 30, unique = true)
    private String loginId;

    @Column(name = "password", nullable = false, length = 100)
    private String password;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

}