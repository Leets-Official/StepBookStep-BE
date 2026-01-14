package com.stepbookstep.server.domain.auth.application.dto

import jakarta.validation.constraints.NotBlank

data class KakaoLoginRequest(
    @field:NotBlank
    val socialToken: String,
    val fcmToken: String? = null // 사용자 디바이스 정보 저장 및 갱산에 활용할 수 있습니다.
)
