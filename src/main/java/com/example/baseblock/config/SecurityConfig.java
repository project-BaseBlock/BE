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
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
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
    private  final UserDetailsService userDetailsService; // CustomUserDetailsService로 주입됨

    // 1. PasswordEncoder 빈 등록
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. AuthenricateionManager 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
            throws Exception {
        return configuration.getAuthenticationManager();
    }

    // 3. AuthenticationProvider 등록
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService); // 직접 만든 CustomUserDetailsService
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // 4. Security Filter Chain 설정 (Spring Security 6 방식임)
    @Bean
    public SecurityFilterChain filterChain(org.springframework.security.config.annotation.web.builders.HttpSecurity http) throws Exception {
        return http
                .httpBasic(b -> b.disable())
                .csrf(c -> c.disable())
                .cors(c -> c.configurationSource(corsConfigurationSource()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // ======================= 공개 엔드포인트 =======================
                        // 로그인/회원가입/로그아웃
                        .requestMatchers(HttpMethod.POST,
                                "/user/login", "/login",
                                "/user/register", "/user/signup",
                                "/user/logout", "/logout"
                        ).permitAll()

                        // CORS 프리플라이트
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Swagger & 공개 API
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                // 일정/좌석(조회만)
                                "/api/schedule", "/api/schedule/**",
                                "/api/seats/**",
                                // 결제/예약 공개 엔드포인트(프로젝트 정책에 맞게)
                                "/api/payments/**", "/payments/**",
                                "/api/reservations"
                        ).permitAll()

                        // ===== 게시판: 조회만 공개 =====
                        .requestMatchers(HttpMethod.GET, "/posts", "/posts/**", "/comments/**").permitAll()
                        // ===== 게시판: 작성/수정/삭제는 인증 필요 (세부 권한은 @PreAuthorize에서 처리) =====
                        .requestMatchers(HttpMethod.POST,   "/posts/new").authenticated()
                        .requestMatchers(HttpMethod.PUT,    "/posts/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/posts/**").authenticated()

                        // ===== Ticket (보호) =====
                        .requestMatchers(HttpMethod.GET,
                                "/tickets/me",
                                "/tickets/by-reservation/*",
                                "/tickets/*"   // /tickets/{ticketId}
                        ).hasAnyRole("USER","ADMIN","MASTER")
                        .requestMatchers(HttpMethod.POST, "/tickets/*/claim")
                        .hasAnyRole("USER","ADMIN","MASTER")

                        // ===== Admin =====
                        .requestMatchers("/admin/posts/**").hasAnyRole("ADMIN","MASTER")
                        .requestMatchers("/admin/users/**").hasRole("MASTER")

                        // 그 외는 인증 필요
                        .anyRequest().authenticated()
                )
                // JWT 필터
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /*
    | 항목                            | 설명                                             |
    | ------------------------------- | -------------------------------------------------|
    | `JwtAuthenticationFilter`       | 직접 만든 JWT 필터 등록                          |
    | `passwordEncoder()`             | 패스워드 암호화를 위한 필수                      |
    | `authenticationProvider()`      | 우리가 만든 `CustomUserDetailsService` 등록      |
    | `.permitAll()`                  | 회원가입/로그인/Swagger 등은 인증 없이 접근 허용 |
    | `.anyRequest().authenticated()` | 나머지 요청은 인증 필요                          |

    */

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
