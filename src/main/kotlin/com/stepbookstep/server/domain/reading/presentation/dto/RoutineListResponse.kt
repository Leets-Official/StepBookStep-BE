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
    val bookTotalPages: Int,
    val bookStatus: UserBookStatus,
    val period: GoalPeriod,
    val metric: GoalMetric,
    val targetAmount: Int,
    val currentProgress: Int,  // 책 전체 대비 진행률 (0-100)
    val remainingPages: Int    // 남은 페이지 수
) {
    companion object {
        fun from(
            goal: ReadingGoal,
            bookTitle: String,
            bookAuthor: String,
            bookCoverImage: String?,
            bookTotalPages: Int,
            bookStatus: UserBookStatus,
            currentProgress: Int
        ): RoutineItem {
            val readPages = (bookTotalPages * currentProgress / 100.0).toInt()
            val remainingPages = (bookTotalPages - readPages).coerceAtLeast(0)

            return RoutineItem(
                goalId = goal.id,
                bookId = goal.bookId,
                bookTitle = bookTitle,
                bookAuthor = bookAuthor,
                bookCoverImage = bookCoverImage,
                bookTotalPages = bookTotalPages,
                bookStatus = bookStatus,
                period = goal.period,
                metric = goal.metric,
                targetAmount = goal.targetAmount,
                currentProgress = currentProgress,
                remainingPages = remainingPages
            )
        }
    }
}