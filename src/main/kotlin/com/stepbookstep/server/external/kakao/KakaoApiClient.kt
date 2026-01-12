package com.stepbookstep.server.external.kakao

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

/**
 * 카카오 access token(socialToken)으로 카카오 사용자 정보를 조회합니다.
 * 서버는 socialToken을 DB에 저장하지 않습니다.
 */

@Component
class KakaoApiClient(
    private val kakaoWebClient: WebClient
) {

    fun getMe(accessToken: String): KakaoUserMeResponse {
        return kakaoWebClient.get()
            .uri("/v2/user/me")
            .header("Authorization", "Bearer $accessToken")
            .retrieve()
            .bodyToMono(KakaoUserMeResponse::class.java)
            .block() ?: throw RuntimeException("Kakao /v2/user/me response is null")
    }
}

data class KakaoUserMe(
    val kakaoId: Long,
    val nickname: String?,
    val profileImageUrl: String?
)
