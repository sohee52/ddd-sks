package com.back.boundedContext.member.out;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class MemberApiClient {
    // RestClient 생성 - HTTP 요청을 보내는 클라이언트
    private final RestClient restClient = RestClient.builder()
            .baseUrl("http://localhost:8080/member/api/v1") // 기본 URL 설정
            .build();

    public String getRandomSecureTip() {
        return restClient.get() // GET 요청
                .uri("/members/randomSecureTip") // 엔드포인트 경로
                .retrieve() // 응답 받기
                .body(String.class); // 응답 본문을 String으로 변환
    }
}