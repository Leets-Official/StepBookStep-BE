package com.stepbookstep.server.domain.onboarding.domain.enum

import io.swagger.v3.oas.annotations.media.Schema

/**
 * 온보딩 레벨 측정을 위한 독서 시간 측정 질문 enum
 */
@Schema(description = """
레벨측정-2: 독서 지속 시간/스타일 (amount 토큰 생성)

매핑:
- SHORT_CHUNKS -> '10분'
- ONE_CHAPTER -> '20쪽'
- READ_LONG_TIME -> '20분'
- IT_DEPENDS -> '10분'
""")
enum class ReadingDurationAnswer {
    SHORT_CHUNKS, // 짧게 끊어 읽는 게 좋아요 -> 10분
    ONE_CHAPTER, // 한 챕터 정도는 괜찮아요 -> 20쪽
    READ_LONG_TIME, // 한 번 잡으면 꽤 오래 읽어요 -> 20분
    IT_DEPENDS // 그때그때 달라요 -> 10분
}