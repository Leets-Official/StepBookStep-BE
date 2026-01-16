package com.stepbookstep.server.domain.reading.presentation.dto

import com.stepbookstep.server.domain.reading.domain.GoalMetric
import com.stepbookstep.server.domain.reading.domain.GoalPeriod

/**
 * 독서 목표 생성/수정/삭제 요청
 *
 * - 삭제: delete = true (다른 필드는 무시됨)
 * - 생성/수정: period, metric, targetAmount 모두 필수
 */
data class UpsertReadingGoalRequest(
    val period: GoalPeriod? = null,
    val metric: GoalMetric? = null,
    val targetAmount: Int? = null,
    val delete: Boolean = false
)