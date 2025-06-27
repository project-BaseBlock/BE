package com.example.baseblock.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .httpBasic(httpBasic -> httpBasic.disable())
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {}) // CORS 설정은 따로 커스터마이징 가능
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/user/**", "/login", "/swagger-ui/**", "/v3/api-docs/**").permitAll() //Swagger 쓰려면 /swagger-ui/**, /v3/api-docs/**는 반드시 열어줘야 함
                        .requestMatchers("/posts", "/posts/*", "/comments/*").permitAll()
                        .requestMatchers("/admin/posts/**").hasAnyRole("ADMIN", "MASTER")
                        .requestMatchers("/admin/users/**").hasRole("MASTER")
                        .requestMatchers("/posts/new", "/comments/new").hasAnyRole("USER", "ADMIN", "MASTER")
                        .requestMatchers("/api/secure-test").authenticated()
                        .anyRequest().authenticated())
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

}
