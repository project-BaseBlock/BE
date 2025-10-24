package com.example.baseblock.board.service;

import com.example.baseblock.board.dto.PostCreateRequest;
import com.example.baseblock.board.dto.PostResponse;
import com.example.baseblock.board.dto.PostUpdateRequest;
import com.example.baseblock.board.entity.Post;
import com.example.baseblock.board.repository.PostRepository;
import com.example.baseblock.user.entity.User;
import com.example.baseblock.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<PostResponse> findAll() {
        return postRepository.findAll().stream()
                .map(PostResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PostResponse findById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. id=" + id));
        return PostResponse.fromEntity(post);
    }

    // 게시글 작성
    @Transactional
    public void createPost(PostCreateRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .author(user)
                .build();

        postRepository.save(post);
    }

    // SpEL에서 사용: 작성자 본인 여부
    @Transactional(readOnly = true)
    public boolean isPostAuthor(Long postId, String email) {
        return postRepository.existsByIdAndAuthor_Email(postId, email);
    }

    // 수정: 작성자 or ADMIN/MASTER
    @Transactional
    public void updatePost(Long postId, PostUpdateRequest request, String email) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        if (!isOwnerOrAdmin(email, post)) {
            throw new AccessDeniedException("수정 권한이 없습니다.");
        }
        post.update(request.getTitle(), request.getContent());
    }

    // 삭제: 하드딜리트, 작성자 or ADMIN/MASTER
    @Transactional
    public void deletePost(Long postId, String email) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다."));

        if (!isOwnerOrAdmin(email, post)) {
            throw new AccessDeniedException("삭제 권한이 없습니다.");
        }

        postRepository.delete(post); // 하드 삭제
    }

    private boolean isOwnerOrAdmin(String email, Post post) {
        boolean owner = post.getAuthor() != null && email.equals(post.getAuthor().getEmail());
        boolean admin = hasAdminOrMaster();
        return owner || admin;
    }

    private boolean hasAdminOrMaster() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream().anyMatch(a -> {
            String r = a.getAuthority();
            return "ROLE_ADMIN".equals(r) || "ROLE_MASTER".equals(r);
        });
    }
}
