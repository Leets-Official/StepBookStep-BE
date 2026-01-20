package com.stepbookstep.server.security.config

import com.stepbookstep.server.security.jwt.AuthenticationInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * 인증 인터셉터를 등록하고,
 * 적용/제외할 API 경로를 설정하는 Web MVC 설정 클래스
 */
@Configuration
class WebConfig(
    private val authenticationInterceptor: AuthenticationInterceptor
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(authenticationInterceptor)
            // 헬스체크, 스웨거 제외 userId 여부로 판단됩니다.
            .excludePathPatterns("/health", "/swagger-ui/**", "/v3/api-docs/**")
    }
}