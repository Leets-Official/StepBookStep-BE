package com.stepbookstep.server.domain.reading.presentation.dto

import com.stepbookstep.server.domain.reading.domain.GoalMetric
import com.stepbookstep.server.domain.reading.domain.GoalPeriod
import jakarta.validation.constraints.Positive

/**
 * 독서 목표 수정 요청
 * - 변경할 필드만 전달
 * - 전달하지 않은 필드는 기존 값 유지
 */
data class UpdateReadingGoalRequest(
    val period: GoalPeriod? = null,
    val metric: GoalMetric? = null,

    @field:Positive(message = "targetAmount는 1 이상이어야 합니다")
    val targetAmount: Int? = null
)