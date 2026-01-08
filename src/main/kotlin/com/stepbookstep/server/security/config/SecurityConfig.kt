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

                // **우선 전부 허용**
                auth.anyRequest().permitAll()
            }

        return http.build()
    }
}
