package com.example.baseblock.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    // 화이트리스트: 인증 없이 접근 허용(permitAll)되는 경로는 필터 스킵
    // ※ 보호가 필요한 경로(예: /posts/new, /comments/new, /api/reservations)는 넣지 않는다!
    private static final List<String> WHITELIST = List.of(
            "/payments/**",
            "/api/payments/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/actuator/**",
            "/user/**",
            "/login",
            "/posts", "/posts/*",
            "/comments/*",
            "/api/schedule/**",
            "/api/seats/**"
    );

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // CORS preflight(OPTIONS)는 항상 스킵
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String uri = request.getRequestURI();
        // 화이트리스트 경로면 스킵
        return WHITELIST.stream().anyMatch(p -> pathMatcher.match(p, uri));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Authorization 헤더가 없거나 Bearer가 아니면: 에러 내지 말고 그대로 통과
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        // 흔히 클라이언트에서 "Bearer null"이 오는 실수 처리
        if (token.isBlank() || "null".equalsIgnoreCase(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            if (jwtTokenProvider.validateToken(token)) {
                Authentication authentication = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                // 유효하지 않은 토큰이면 인증 컨텍스트만 비우고 계속 진행
                SecurityContextHolder.clearContext();
            }
        } catch (Exception ex) {
            // 파싱/검증 중 예외가 나도 401/403을 여기서 직접 내지 않는다
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
