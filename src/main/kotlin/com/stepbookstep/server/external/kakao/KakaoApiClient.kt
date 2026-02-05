package com.stepbookstep.server.external.kakao

import com.stepbookstep.server.domain.auth.application.dto.KakaoTokenResponse
import com.stepbookstep.server.global.response.CustomException
import com.stepbookstep.server.global.response.ErrorCode
import com.stepbookstep.server.security.jwt.KakaoProperties
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import org.springframework.http.MediaType
/**
 * 카카오 access token(socialToken)으로 카카오 사용자 정보를 조회합니다.
 * 서버는 socialToken을 DB에 저장하지 않습니다.
 */

@Component
class KakaoApiClient(
    private val kakaoWebClient: WebClient,
    private val kakaoProperties: KakaoProperties,
    private val kakaoAuthWebClient: WebClient
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

    fun getToken(code: String, redirectUri: String): KakaoTokenResponse {
        return kakaoAuthWebClient.post()
            .uri("/oauth/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(
                BodyInserters.fromFormData("grant_type", "authorization_code")
                    .with("client_id", kakaoProperties.clientId)
                    .with("client_secret", kakaoProperties.clientSecret)
                    .with("redirect_uri", redirectUri)
                    .with("code", code)
            )
            .retrieve()
            .bodyToMono(KakaoTokenResponse::class.java)
            .block()!!
    }
}

data class KakaoUserMe(
    val kakaoId: Long,
    val nickname: String?,
    val profileImageUrl: String?
)
