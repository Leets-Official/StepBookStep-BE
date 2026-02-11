package com.stepbookstep.server.domain.reading.presentation.dto

import com.stepbookstep.server.domain.reading.domain.GoalMetric
import com.stepbookstep.server.domain.reading.domain.GoalPeriod
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

/**
 * 독서 목표 생성 요청
 */
data class CreateReadingGoalRequest(
    @field:NotNull(message = "period는 필수입니다")
    val period: GoalPeriod,

    @field:NotNull(message = "metric은 필수입니다")
    val metric: GoalMetric,

    @field:NotNull(message = "targetAmount는 필수입니다")
    @field:Positive(message = "targetAmount는 1 이상이어야 합니다")
    val targetAmount: Int
)