package com.back.boundedContext.post.service;

import com.back.boundedContext.member.entity.Member;
import com.back.boundedContext.post.entity.Post;
import com.back.boundedContext.post.repository.PostRepository;
import com.back.global.eventPublisher.EventPublisher;
import com.back.shared.post.dto.PostDto;
import com.back.shared.post.event.PostCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor // final 이 붙은 필드에 대해 생성자를 자동으로 생성
public class PostService {
    private final PostRepository postRepository;
    private final EventPublisher eventPublisher;

    public long count() {
        return postRepository.count();
    }

    public Post write(Member author, String title, String content) {
        Post post = postRepository.save(new Post(author, title, content));

        eventPublisher.publish(
                new PostCreatedEvent(
                        new PostDto(post)
                )
        );

        return post;
    }

    public Optional<Post> findById(int id) {
        return postRepository.findById(id);
    }
}
