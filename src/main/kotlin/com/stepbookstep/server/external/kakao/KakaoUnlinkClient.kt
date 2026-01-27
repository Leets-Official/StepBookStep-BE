package com.stepbookstep.server.external.kakao

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate

/**
 * 카카오 Admin Key를 사용해 카카오 서버에 사용자 연결 해제(unlink)를 요청하는 클라이언트.
 */
@Component
class KakaoUnlinkClient(
    private val restTemplate: RestTemplate,
    @Value("\${kakao.admin-key}")
    private val adminKey: String
) {

    fun unlink(kakaoUserId: String) {
        val url = "https://kapi.kakao.com/v1/user/unlink"

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_FORM_URLENCODED
            set("Authorization", "KakaoAK $adminKey")
        }

        val body = LinkedMultiValueMap<String, String>().apply {
            add("target_id_type", "user_id")
            add("target_id", kakaoUserId)
        }

        val request = HttpEntity(body, headers)

        restTemplate.postForEntity(url, request, String::class.java)
    }
}