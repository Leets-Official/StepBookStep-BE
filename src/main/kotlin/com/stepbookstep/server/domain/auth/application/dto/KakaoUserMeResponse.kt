package com.stepbookstep.server.domain.auth.application.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class KakaoUserMeResponse(
    val id: Long,

    @JsonProperty("kakao_account")
    val kakaoAccount: KakaoAccount?
) {
    val nickname: String?
        get() = kakaoAccount?.profile?.nickname

    val email: String?
        get() = kakaoAccount?.email
}

data class KakaoAccount(
    val profile: KakaoProfile?,
    val email: String?
)

data class KakaoProfile(
    val nickname: String?
)