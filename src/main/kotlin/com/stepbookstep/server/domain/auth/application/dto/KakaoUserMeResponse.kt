package com.stepbookstep.server.domain.auth.application.dto

data class KakaoUserMeResponse(
    val id: Long,
    val kakaoAccount: KakaoAccount?
) {
    val nickname: String?
        get() = kakaoAccount?.profile?.nickname
}

data class KakaoAccount(
    val profile: KakaoProfile?
)

data class KakaoProfile(
    val nickname: String?
)