package com.stepbookstep.server.domain.onboarding.domain.enum

/**
 * 온보딩 레벨 측정을 위한 도서 선택 기준 질문 enum
 */
enum class DifficultyPreferenceAnswer {
    THICK_OR_HARD_BOOK, // 두껍거나 어려워보이는 책 -> 얇은 책
    HARD_TO_UNDERSTAND, // 무슨 말인지 잘 안 들어오는 문장 -> 장르 분류에서 선택하게 함
    PRESSURE_TO_FINISH, //레벨 별 추천도서
    NO_BURDEN // 레벨 별 추천도서
}