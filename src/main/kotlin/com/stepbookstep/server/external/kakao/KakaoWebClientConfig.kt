package com.stepbookstep.server.external.kakao

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

/**
 * 카카오 API 통신을 위한 WebClient 설정 클래스
 */
@Configuration
class KakaoWebClientConfig {

    @Bean
    fun kakaoWebClient(): WebClient {
        return WebClient.builder()
            // 카카오 API의 기본 주소를 설정합니다.
            .baseUrl("https://kapi.kakao.com")
            .build()
    }

    @Bean
    fun kakaoAuthWebClient(): WebClient {
        return WebClient.builder()
            .baseUrl("https://kauth.kakao.com")
            .build()
    }
}
