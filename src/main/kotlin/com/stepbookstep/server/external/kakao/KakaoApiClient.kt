package com.stepbookstep.server.external.kakao

import com.stepbookstep.server.global.response.CustomException
import com.stepbookstep.server.global.response.ErrorCode
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

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
            // 4xx 에러 (토큰 만료, 잘못된 토큰 등) 처리
            .onStatus({ it.is4xxClientError }) { _ ->
                Mono.error(CustomException(ErrorCode.TOKEN_INVALID))
            }
            // 5xx 에러 (카카오 서버 장애) 처리
            .onStatus({ it.is5xxServerError }) { _ ->
                Mono.error(CustomException(ErrorCode.INTERNAL_SERVER_ERROR))
            }
            .bodyToMono(KakaoUserMeResponse::class.java)
            .block() ?: throw CustomException(ErrorCode.INTERNAL_SERVER_ERROR)
    }
}

data class KakaoUserMe(
    val kakaoId: Long,
    val nickname: String?,
    val profileImageUrl: String?
)
