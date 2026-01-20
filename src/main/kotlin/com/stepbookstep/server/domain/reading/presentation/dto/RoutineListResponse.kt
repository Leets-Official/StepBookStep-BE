package com.stepbookstep.server.domain.reading.presentation.dto

import com.stepbookstep.server.domain.reading.domain.GoalMetric
import com.stepbookstep.server.domain.reading.domain.GoalPeriod
import com.stepbookstep.server.domain.reading.domain.ReadingGoal
import com.stepbookstep.server.domain.reading.domain.UserBookStatus

/**
 * 루틴 목록 조회 응답
 * - 활성화된 독서 목표 리스트
 */
data class RoutineListResponse(
    val routines: List<RoutineItem>
)

data class RoutineItem(
    val goalId: Long,
    val bookId: Long,
    val bookTitle: String,
    val bookAuthor: String,
    val bookCoverImage: String?,
    val bookPublisher: String?,
    val bookPublishYear: Int?,
    val bookTotalPages: Int,
    val bookStatus: UserBookStatus,
    val period: GoalPeriod,
    val metric: GoalMetric,
    val targetAmount: Int,
    val achievedAmount: Int,      // 현재 기간에 달성한 양 (페이지 또는 분)
    val remainingAmount: Int      // 목표 달성까지 남은 양 (페이지 또는 분)
) {
    companion object {
        fun from(
            goal: ReadingGoal,
            bookTitle: String,
            bookAuthor: String,
            bookCoverImage: String?,
            bookPublisher: String?,
            bookPublishYear: Int?,
            bookTotalPages: Int,
            bookStatus: UserBookStatus,
            currentProgress: Int,
            achievedAmount: Int
        ): RoutineItem {
            val remainingAmount = (goal.targetAmount - achievedAmount).coerceAtLeast(0)

            return RoutineItem(
                goalId = goal.id,
                bookId = goal.bookId,
                bookTitle = bookTitle,
                bookAuthor = bookAuthor,
                bookCoverImage = bookCoverImage,
                bookPublisher = bookPublisher,
                bookPublishYear = bookPublishYear,
                bookTotalPages = bookTotalPages,
                bookStatus = bookStatus,
                period = goal.period,
                metric = goal.metric,
                targetAmount = goal.targetAmount,
                achievedAmount = achievedAmount,
                remainingAmount = remainingAmount
            )
        }
    }
}