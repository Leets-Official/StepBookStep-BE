package com.stepbookstep.server.external.kakao

import com.stepbookstep.server.global.response.CustomException
import com.stepbookstep.server.global.response.ErrorCode
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClient

/**
 * 카카오 Admin Key를 사용해 카카오 서버에 사용자 연결 해제(unlink)를 요청하는 클라이언트.
 */
@Component
class KakaoUnlinkClient(
    private val restClient: RestClient,
    @Value("\${kakao.admin-key}")
    private val adminKey: String
) {

    fun unlink(kakaoUserId: String) {
        val url = "https://kapi.kakao.com/v1/user/unlink"

        val body = LinkedMultiValueMap<String, String>().apply {
            add("target_id_type", "user_id")
            add("target_id", kakaoUserId)
        }

        try {
            restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header("Authorization", "KakaoAK $adminKey")
                .body(body)
                .retrieve()
                .toBodilessEntity()
        } catch (e: Exception) {
            throw CustomException(ErrorCode.KAKAO_UNLINK_FAILED)
        }
    }
}