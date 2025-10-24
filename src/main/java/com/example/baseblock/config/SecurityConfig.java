package com.example.baseblock.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@EnableMethodSecurity
@RequiredArgsConstructor
@Configuration
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService; // CustomUserDetailsService로 주입됨

    // 1. PasswordEncoder 빈 등록
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. AuthenticationManager 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
            throws Exception {
        return configuration.getAuthenticationManager();
    }

    // 3. AuthenticationProvider 등록
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // 4. Security Filter Chain 설정
    @Bean
    public SecurityFilterChain filterChain(org.springframework.security.config.annotation.web.builders.HttpSecurity http) throws Exception {
        return http
                .httpBasic(b -> b.disable())
                .csrf(c -> c.disable())
                .cors(c -> c.configurationSource(corsConfigurationSource()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // ======================= 공개 엔드포인트 =======================
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                // ✅ 경기 일정 조회 (GET 전용 허용)
                                "/api/schedule", "/api/schedule/**",
                                // ✅ 좌석 조회
                                "/api/seats/**",
                                // ✅ 결제 조회/콜백 등
                                "/api/payments/**", "/payments/**"
                        ).permitAll()

                        // 로그인/회원가입/로그아웃
                        .requestMatchers(HttpMethod.POST,
                                "/user/login", "/login",
                                "/user/register", "/user/signup",
                                "/user/logout", "/logout"
                        ).permitAll()

                        // ======================= 보호 엔드포인트 =======================
                        // ✅ 예매 생성은 인증 필요
                        .requestMatchers(HttpMethod.POST, "/api/reservations/**").authenticated()

                        // ===== 게시판: 조회만 공개 =====
                        .requestMatchers(HttpMethod.GET, "/posts", "/posts/**", "/comments/**").permitAll()
                        // ===== 게시판: 작성/수정/삭제는 인증 필요 =====
                        .requestMatchers(HttpMethod.POST, "/posts/new").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/posts/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/posts/**").authenticated()

                        // ===== Ticket (보호) =====
                        .requestMatchers(HttpMethod.GET,
                                "/tickets/me",
                                "/tickets/by-reservation/*",
                                "/tickets/*"
                        ).hasAnyRole("USER", "ADMIN", "MASTER")
                        .requestMatchers(HttpMethod.POST, "/tickets/*/claim")
                        .hasAnyRole("USER", "ADMIN", "MASTER")

                        // ===== Admin =====
                        .requestMatchers("/admin/posts/**").hasAnyRole("ADMIN", "MASTER")
                        .requestMatchers("/admin/users/**").hasRole("MASTER")

                        // ======================= 나머지 인증 필요 =======================
                        .anyRequest().authenticated()
                )
                // ✅ JWT 필터
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    // ✅ CORS 설정
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.addAllowedOriginPattern("http://localhost:5173"); // ✅ 최신 방식
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")); // ✅ PATCH 추가
        configuration.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
