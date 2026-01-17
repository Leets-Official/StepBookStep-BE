package com.stepbookstep.server.domain.reading.presentation.dto

import com.stepbookstep.server.domain.reading.domain.GoalMetric
import com.stepbookstep.server.domain.reading.domain.GoalPeriod
import com.stepbookstep.server.domain.reading.domain.ReadingGoal
import java.time.OffsetDateTime

data class ReadingGoalResponse(
    val goalId: Long,
    val bookId: Long,
    val period: GoalPeriod,
    val metric: GoalMetric,
    val targetAmount: Int,
    val currentProgress: Int,        // 책 전체 대비 읽은 비율 (0-100)
    val isActive: Boolean,
    val createdAt: OffsetDateTime
) {
    companion object {
        fun from(
            goal: ReadingGoal,
            currentProgress: Int
        ): ReadingGoalResponse {
            return ReadingGoalResponse(
                goalId = goal.id,
                bookId = goal.bookId,
                period = goal.period,
                metric = goal.metric,
                targetAmount = goal.targetAmount,
                currentProgress = currentProgress,
                isActive = goal.active,
                createdAt = goal.createdAt
            )
        }
    }
}