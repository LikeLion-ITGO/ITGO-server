package likelion.itgoserver.domain.member.repository;


import likelion.itgoserver.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByUsername(String username);
}