package com.stepbookstep.server.domain.onboarding.application.dto

data class NicknameCheckResponse(
    val nickname: String,
    val isAvailable: Boolean
)