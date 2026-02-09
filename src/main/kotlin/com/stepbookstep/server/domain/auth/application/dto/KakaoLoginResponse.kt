package com.stepbookstep.server.domain.auth.application.dto

import com.stepbookstep.server.domain.user.domain.SignupType

data class KakaoLoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val signupType: SignupType,
    val nickname: String?,
    val email: String?
)
