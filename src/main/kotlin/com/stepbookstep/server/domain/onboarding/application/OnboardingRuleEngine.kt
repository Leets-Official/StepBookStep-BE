package com.stepbookstep.server.domain.onboarding.application

import com.stepbookstep.server.domain.onboarding.application.dto.LevelAnswers
import com.stepbookstep.server.domain.onboarding.domain.enum.*
import org.springframework.stereotype.Component

/**
 * 온보딩 설문 응답(LevelAnswers)을 기반으로
 * 사용자 독서 레벨과 추천 루틴 타입을 계산하는 규칙 엔진
 */
@Component
class OnboardingRuleEngine {

    data class Result(
        val level: Int,
        val routineType: RoutineType
    )

    fun calculate(a: LevelAnswers): Result {
        // TODO: 규칙 확정되면 정교화 (현재는 임시로 고정값을 반환합니다.)
        val level = when (a.readingFrequency) {
            ReadingFrequencyAnswer.FINISHED_RECENTLY -> 3
            ReadingFrequencyAnswer.STOP_MIDWAY -> 2
            ReadingFrequencyAnswer.LONG_TIME_NO_BOOK,
            ReadingFrequencyAnswer.DONT_KNOW_START -> 1
        }

        val routineType = when (level) {
            3 -> RoutineType.HARD
            2 -> RoutineType.NORMAL
            else -> RoutineType.LIGHT
        }

        return Result(level, routineType)
    }
}
