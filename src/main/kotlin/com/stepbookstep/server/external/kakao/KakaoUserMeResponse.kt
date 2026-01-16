package com.stepbookstep.server.external.kakao

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 카카오 사용자 정보 가져오기의 응답 데이터를 담는 클래스
 */

data class KakaoUserMeResponse(
    val id: Long,
    @JsonProperty("kakao_account")
    val kakaoAccount: KakaoAccount?
) {
    val nickname: String?
        get() = kakaoAccount?.profile?.nickname

    data class KakaoAccount(
        val profile: Profile? = null
    )

    data class Profile(
        val nickname: String? = null,
        @JsonProperty("profile_image_url")
        val profileImageUrl: String? = null
    )
}
