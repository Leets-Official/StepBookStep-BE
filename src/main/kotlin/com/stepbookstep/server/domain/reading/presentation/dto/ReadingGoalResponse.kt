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
    val currentProgress: Int,        // 현재 진행량
    val achievementRate: Double,     // 달성률 (0.0 ~ 100.0)
    val isActive: Boolean,
    val createdAt: OffsetDateTime
) {
    companion object {
        fun from(
            goal: ReadingGoal,
            currentProgress: Int
        ): ReadingGoalResponse {
            val achievementRate = if (goal.targetAmount > 0) {
                (currentProgress.toDouble() / goal.targetAmount * 100).coerceIn(0.0, 100.0)
            } else {
                0.0
            }

            return ReadingGoalResponse(
                goalId = goal.id,
                bookId = goal.bookId,
                period = goal.period,
                metric = goal.metric,
                targetAmount = goal.targetAmount,
                currentProgress = currentProgress,
                achievementRate = achievementRate,
                isActive = goal.active,
                createdAt = goal.createdAt
            )
        }
    }
}