package com.stepbookstep.server.domain.onboarding.domain.enum

import io.swagger.v3.oas.annotations.media.Schema

/**
 * 온보딩 레벨 측정을 위한 도서 선택 기준 질문 enum
 */
@Schema(description = """
레벨측정-3: 읽기에서 부담되는 요소 (basis 토큰 생성)

매핑:
- THICK_OR_HARD_BOOK -> '얇은 책'
- HARD_TO_UNDERSTAND -> '얇은 책'
- PRESSURE_TO_FINISH -> '레벨 별 추천도서'
- NO_BURDEN -> '레벨 별 추천도서'

단, categoryIds+genreIds가 0개인 경우 basis는 무조건 '레벨 별 추천도서'로 응답합니다.
""")
enum class DifficultyPreferenceAnswer {
    THICK_OR_HARD_BOOK, // 두껍거나 어려워보이는 책 -> 얇은 책
    HARD_TO_UNDERSTAND, // 무슨 말인지 잘 안 들어오는 문장 -> 장르 분류에서 선택하게 함
    PRESSURE_TO_FINISH, //레벨 별 추천도서
    NO_BURDEN // 레벨 별 추천도서
}