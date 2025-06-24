package com.example.baseblock.user.service;

import com.example.baseblock.user.entity.User;
import com.example.baseblock.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // DB에서 사용자 정보 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("해당 사용자가 존재하지 않습니다: " + email));

        // 스프링 시큐리티에서 사용할 UserDetails 객체 생성
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())      // 인증 ID
                .password(user.getPassword())   // 비밀번호
                .roles(user.getRole().name())   // 권한
                .build();
    }
}
