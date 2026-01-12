package com.stepbookstep.server.security.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .authorizeHttpRequests { auth ->
                auth.requestMatchers(
                    "/health",
                    "/swagger-ui/**",
                    "/v3/api-docs/**"
                ).permitAll()

                // 우선 전부 허용 : 현재 개발 단계에서 보안 제약 없이 API를 테스트하기 위함
                auth.anyRequest().permitAll()
            }

        return http.build()
    }
}
