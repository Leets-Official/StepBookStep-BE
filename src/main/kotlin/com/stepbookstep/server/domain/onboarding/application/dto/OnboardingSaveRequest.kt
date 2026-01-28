package com.stepbookstep.server.domain.onboarding.application.dto

import com.stepbookstep.server.domain.onboarding.domain.enum.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class OnboardingSaveRequest(
    @field:NotBlank
    val nickname: String,

    @field:NotNull
    val levelAnswers: LevelAnswers,

    val categoryIds: List<Long> = emptyList(),
    val genreIds: List<Long> = emptyList()
)

data class LevelAnswers(
    val readingFrequency: ReadingFrequencyAnswer,
    val readingDuration: ReadingDurationAnswer,
    val difficultyPreference: DifficultyPreferenceAnswer
)
