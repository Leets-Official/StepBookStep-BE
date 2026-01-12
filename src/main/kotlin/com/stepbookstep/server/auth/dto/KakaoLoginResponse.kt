package com.stepbookstep.server.auth.dto

data class KakaoLoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val isNewUser: Boolean,
    val nickname: String?
)
