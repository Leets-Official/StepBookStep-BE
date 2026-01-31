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

    data class Tokens(
        val periodToken: String,
        val amountToken: String,
        val basisToken: String
    )

    fun createTokens(
        answers: LevelAnswers,
        preferenceCount: Int,
    ): Tokens {

        val periodToken = when (answers.readingFrequency) {
            ReadingFrequencyAnswer.FINISHED_RECENTLY,
            ReadingFrequencyAnswer.STOP_MIDWAY -> "하루"

            ReadingFrequencyAnswer.LONG_TIME_NO_BOOK,
            ReadingFrequencyAnswer.DONT_KNOW_START -> "일주일"
        }

        val amountToken = when (answers.readingDuration) {
            ReadingDurationAnswer.SHORT_CHUNKS,
            ReadingDurationAnswer.IT_DEPENDS -> "10분"
            ReadingDurationAnswer.ONE_CHAPTER -> "20쪽"
            ReadingDurationAnswer.READ_LONG_TIME -> "20분"
        }

        var basisToken = when (answers.difficultyPreference) {
            DifficultyPreferenceAnswer.THICK_OR_HARD_BOOK,
            DifficultyPreferenceAnswer.HARD_TO_UNDERSTAND -> "얇은 책"

            DifficultyPreferenceAnswer.PRESSURE_TO_FINISH,
            DifficultyPreferenceAnswer.NO_BURDEN -> "레벨 별 추천도서"
        }

        // 선호 분류 0개면 무조건 레벨 추천
        if (preferenceCount == 0) {
            basisToken = "레벨 별 추천도서"
        }

        return Tokens(periodToken, amountToken, basisToken)
    }
}