package com.stepbookstep.server.domain.reading.presentation.dto

import com.stepbookstep.server.domain.mypage.domain.ReadStatus
import com.stepbookstep.server.domain.reading.domain.GoalMetric
import com.stepbookstep.server.domain.reading.domain.GoalPeriod
import com.stepbookstep.server.domain.reading.domain.ReadingGoal
import com.stepbookstep.server.domain.reading.domain.ReadingLog
import java.time.LocalDate

/**
 * 독서 상세 페이지
 */
data class BookReadingDetailResponse(
    val bookStatus: ReadStatus,
    val goal: GoalInfo?,

    val currentPage: Int,
    val totalPages: Int,
    val progressPercent: Int,

    val startDate: LocalDate?,
    val endDate: LocalDate?,

    val rating: Int?,

    val readingLogs: List<ReadingLogItem>
)

data class GoalInfo(
    val goalId: Long,
    val period: GoalPeriod,
    val metric: GoalMetric,
    val targetAmount: Int,
    val isActive: Boolean
) {
    companion object {
        fun from(goal: ReadingGoal): GoalInfo {
            return GoalInfo(
                goalId = goal.id,
                period = goal.period,
                metric = goal.metric,
                targetAmount = goal.targetAmount,
                isActive = goal.active
            )
        }
    }
}

data class ReadingLogItem(
    val logId: Long,
    val recordDate: LocalDate,
    val readQuantity: Int?,
    val progressPercent: Int?,
    val durationSeconds: Int?
) {
    companion object {
        fun from(log: ReadingLog, totalPages: Int, goalMetric: GoalMetric?): ReadingLogItem {
            val quantity = log.readQuantity
            val percent = if (totalPages > 0 && quantity != null) {
                ((quantity.toDouble() / totalPages) * 100).toInt().coerceIn(0, 100)
            } else null

            // PAGE 목표면 시간 제외, TIME 목표면 시간 포함
            val duration = if (goalMetric == GoalMetric.TIME) log.durationSeconds else null

            return ReadingLogItem(
                logId = log.id,
                recordDate = log.recordDate,
                readQuantity = log.readQuantity,
                progressPercent = percent,
                durationSeconds = duration
            )
        }
    }
}