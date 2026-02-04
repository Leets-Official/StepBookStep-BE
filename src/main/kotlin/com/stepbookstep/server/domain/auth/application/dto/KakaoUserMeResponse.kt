package com.stepbookstep.server.domain.auth.application.dto

data class KakaoUserMeResponse(
    val id: Long,
    val kakao_account: KakaoAccount?
) {
    val nickname: String?
        get() = kakao_account?.profile?.nickname
}

data class KakaoAccount(
    val profile: KakaoProfile?
)

data class KakaoProfile(
    val nickname: String?
)