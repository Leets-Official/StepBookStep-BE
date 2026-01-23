package com.stepbookstep.server.security.config

import com.stepbookstep.server.security.jwt.AuthenticationInterceptor
import com.stepbookstep.server.security.jwt.LoginUserIdArgumentResolver
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * 인증 인터셉터를 등록하고,
 * 적용/제외할 API 경로를 설정하는 Web MVC 설정 클래스
 */
@Configuration
class WebConfig(
    private val authenticationInterceptor: AuthenticationInterceptor,
    private val authenticationPrincipalArgumentResolver: LoginUserIdArgumentResolver
) : WebMvcConfigurer {

    companion object {
        // 인증 불필요 (공개 API)
        private val PUBLIC_PATHS = arrayOf(
            "/health",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/api/v1/auth/**"
        )
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(authenticationPrincipalArgumentResolver)
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        // 모든 API에 필수 인증 적용 (PUBLIC_PATHS 제외)
        registry.addInterceptor(authenticationInterceptor)
            .addPathPatterns("/api/**")
            .excludePathPatterns(*PUBLIC_PATHS)
    }
}
