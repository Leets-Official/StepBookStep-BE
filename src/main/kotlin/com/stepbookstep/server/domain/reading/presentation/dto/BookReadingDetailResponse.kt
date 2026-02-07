package com.stepbookstep.server.domain.reading.presentation.dto

import com.stepbookstep.server.domain.mypage.domain.ReadStatus
import com.stepbookstep.server.domain.reading.domain.GoalMetric
import com.stepbookstep.server.domain.reading.domain.GoalPeriod
import com.stepbookstep.server.domain.reading.domain.ReadingGoal
import java.time.LocalDate

/**
 * 독서 상세 페이지 응답
 */
data class BookReadingDetailResponse(
    // 도서 상태 & 목표
    val bookStatus: ReadStatus,
    val goal: GoalInfo?,

    // 현재 진도
    val currentPage: Int,
    val totalPages: Int,
    val progressPercent: Int,

    // 시작일/종료일
    val startDate: LocalDate?,
    val endDate: LocalDate?,

    // 별점 (FINISHED/STOPPED일 때만)
    val rating: Int?,

    // 독서 기록 리스트 (날짜 내림차순)
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
    val pagesRead: Int?,
    val durationSeconds: Int?
)