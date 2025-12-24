package com.back.boundedContext.member.app;

import com.back.boundedContext.member.domain.Member;
import com.back.boundedContext.member.out.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberFacade {
    private final MemberRepository memberRepository;
    private final MemberJoinUseCase memberJoinUseCase;

    @Transactional(readOnly = true)
    public long count() {
        return memberRepository.count(); // count는 JPARepository에서 제공하는 기본 메서드로, 총 엔티티 수를 반환합니다.
    }

    @Transactional
    public Member join(String username, String password, String nickname) {
        return memberJoinUseCase.join(username, password, nickname);
    }

    @Transactional(readOnly = true)
    public Optional<Member> findByUsername(String username) {
        return memberRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public Optional<Member> findById(int id) {
        return memberRepository.findById(id);
    }
}
