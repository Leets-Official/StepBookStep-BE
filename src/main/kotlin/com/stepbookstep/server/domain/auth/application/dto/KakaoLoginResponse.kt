package com.stepbookstep.server.domain.auth.application.dto

data class KakaoLoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val isNewUser: Boolean,
    val nickname: String?
)
