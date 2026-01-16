package com.stepbookstep.server.domain.reading.presentation.dto

import com.stepbookstep.server.domain.reading.domain.GoalMetric
import com.stepbookstep.server.domain.reading.domain.GoalPeriod

data class ActiveReadingGoalResponse(
    val goalId: Long,
    val bookId: Long,
    val bookTitle: String,
    val bookAuthor: String,
    val period: GoalPeriod,
    val metric: GoalMetric,
    val targetAmount: Int,
    val currentProgress: Int,
    val achievementRate: Double
)