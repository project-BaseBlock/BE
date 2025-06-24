package com.example.baseblock.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.In;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {

        // 1. Security 설정 추가
        SecurityScheme bearerAuth = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(In.HEADER) // 추가: 헤더에 들어가야 한다는 의미
                .name("Authorization");       // 추가: Swagger Authorize 버튼에 표시할 이름

        // 2. Security 적용 대상 설정
        SecurityRequirement securityRequirement = new SecurityRequirement().addList("BearerAuth");

        // 3. 최종 OpenAPI 반환
        return  new OpenAPI()
                .components(new Components().addSecuritySchemes("BearerAuth", bearerAuth))
                .addSecurityItem(securityRequirement)
                .info(new Info()
                        .title("BaseBlock API")
                        .description("야구 예매 사이트 API 명세서")
                        .version("v1.0.0"));
    }
}
