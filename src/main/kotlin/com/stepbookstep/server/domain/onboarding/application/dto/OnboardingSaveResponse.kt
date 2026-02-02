package com.stepbookstep.server.domain.onboarding.application.dto

data class OnboardingSaveResponse(
    val isOnboarded: Boolean,
    val level: Int,
    val routineTokens: RoutineTokens
)

data class RoutineTokens(
    val period: String, // "하루" / "일주일"
    val amount: String, // "10분" / "20쪽" / "20분"
    val basis: String   // "얅은 책" / "레벨 별 추천도서" / "선호 분류"
)