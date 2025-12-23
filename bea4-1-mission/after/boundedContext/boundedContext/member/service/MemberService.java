package com.back.boundedContext.member.service;

import com.back.boundedContext.member.entity.Member;
import com.back.global.exception.DomainException;
import com.back.boundedContext.member.repository.MemberRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MemberService {
    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public long count() {
        return memberRepository.count(); // count는 JPARepository에서 제공하는 기본 메서드로, 총 엔티티 수를 반환합니다.
    }

    public Member join(String username, String password, String nickname) {
        memberRepository.findByUsername(username).ifPresent(m -> {
            throw new DomainException("409-1", "이미 존재하는 username 입니다."); // 프론트에서 활용할 수 있도록 적절한 코드와 메시지를 설정합니다.
        });

        return memberRepository.save(new Member(username, password, nickname)); // save는 JPARepository에서 제공하는 기본 메서드로, 엔티티를 저장합니다.
    }

    public Optional<Member> findByUsername(String username) {
        return memberRepository.findByUsername(username);
    }
}
