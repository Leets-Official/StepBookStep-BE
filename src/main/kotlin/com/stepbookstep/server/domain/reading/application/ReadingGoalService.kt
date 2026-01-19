package com.stepbookstep.server.domain.reading.application

import com.stepbookstep.server.domain.book.domain.BookRepository
import com.stepbookstep.server.domain.reading.domain.GoalMetric
import com.stepbookstep.server.domain.reading.domain.GoalPeriod
import com.stepbookstep.server.domain.reading.domain.ReadingGoal
import com.stepbookstep.server.domain.reading.domain.ReadingGoalRepository
import com.stepbookstep.server.domain.reading.domain.ReadingLogRepository
import com.stepbookstep.server.domain.reading.domain.UserBook
import com.stepbookstep.server.domain.reading.domain.UserBookRepository
import com.stepbookstep.server.domain.reading.domain.UserBookStatus
import com.stepbookstep.server.global.response.CustomException
import com.stepbookstep.server.global.response.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.OffsetDateTime

@Service
class ReadingGoalService(
    private val readingGoalRepository: ReadingGoalRepository,
    private val userBookRepository: UserBookRepository,
    private val bookRepository: BookRepository,
    private val readingLogRepository: ReadingLogRepository
) {

    /**
     * 목표 생성 또는 수정 (Upsert)
     * - 기존 활성 목표가 없으면 새로 생성
     * - 기존 활성 목표가 있으면 수정
     */
    @Transactional
    fun upsertGoal(
        userId: Long,
        bookId: Long,
        period: GoalPeriod,
        metric: GoalMetric,
        targetAmount: Int
    ): ReadingGoal {
        if (!bookRepository.existsById(bookId)) {
            throw CustomException(ErrorCode.BOOK_NOT_FOUND)
        }

        // UserBook 생성 또는 상태 업데이트
        val userBook = userBookRepository.findByUserIdAndBookId(userId, bookId)
            ?: UserBook(
                userId = userId,
                bookId = bookId,
                status = UserBookStatus.READING
            )

        if (userBook.status != UserBookStatus.READING) {
            userBook.status = UserBookStatus.READING
            userBook.updatedAt = OffsetDateTime.now()
        }

        userBookRepository.save(userBook)


        // 기존 활성 목표 조회
        val existingGoal = readingGoalRepository.findByUserIdAndBookIdAndActiveTrue(userId, bookId)

        return if (existingGoal != null) {
            // 수정: 기존 목표의 속성 업데이트
            existingGoal.apply {
                this.period = period
                this.metric = metric
                this.targetAmount = targetAmount
                this.updatedAt = OffsetDateTime.now()
            }
            readingGoalRepository.save(existingGoal)
        } else {
            // 생성: 새로운 목표 저장
            readingGoalRepository.save(
                ReadingGoal(
                    userId = userId,
                    bookId = bookId,
                    period = period,
                    metric = metric,
                    targetAmount = targetAmount,
                    active = true
                )
            )
        }
    }

    /**
     * 활성 목표 삭제 (비활성화)
     */
    @Transactional
    fun deleteGoal(userId: Long, bookId: Long) {
        val existingGoal = readingGoalRepository.findByUserIdAndBookIdAndActiveTrue(userId, bookId)
            ?: throw CustomException(ErrorCode.GOAL_NOT_FOUND)

        existingGoal.active = false
        existingGoal.updatedAt = OffsetDateTime.now()
        readingGoalRepository.save(existingGoal)
    }

    /**
     * 특정 책의 활성 목표 조회 (진행률 포함)
     */
    @Transactional(readOnly = true)
    fun getActiveGoalWithProgress(userId: Long, bookId: Long): ReadingGoalWithProgress? {
        val goal = readingGoalRepository.findByUserIdAndBookIdAndActiveTrue(userId, bookId)
            ?: return null

        val book = bookRepository.findById(bookId).orElseThrow {
            CustomException(ErrorCode.BOOK_NOT_FOUND)
        }

        val currentProgress = calculateCurrentProgress(userId, bookId, goal.period, goal.metric, book.itemPage)

        return ReadingGoalWithProgress(
            goal = goal,
            currentProgress = currentProgress
        )
    }

    /**
     * 특정 책의 목표 조회 (활성/비활성 무관, 진행률 포함)
     * - 완독/중지 상태에서도 목표를 표시하기 위해 사용
     * - 가장 최근 목표를 조회
     */
    @Transactional(readOnly = true)
    fun getGoalWithProgress(userId: Long, bookId: Long): ReadingGoalWithProgress? {
        val goal = readingGoalRepository.findTopByUserIdAndBookIdOrderByCreatedAtDesc(userId, bookId)
            ?: return null

        val book = bookRepository.findById(bookId).orElseThrow {
            CustomException(ErrorCode.BOOK_NOT_FOUND)
        }

        val currentProgress = calculateCurrentProgress(userId, bookId, goal.period, goal.metric, book.itemPage)

        return ReadingGoalWithProgress(
            goal = goal,
            currentProgress = currentProgress
        )
    }

    /**
     * 현재 진행률 계산 (책 전체 대비 읽은 비율 0-100)
     */
    private fun calculateCurrentProgress(
        userId: Long,
        bookId: Long,
        period: GoalPeriod,
        metric: GoalMetric,
        totalPages: Int
    ): Int {
        // 전체 누적 읽은 페이지 수
        val readPages = readingLogRepository.sumTotalReadQuantityByUserIdAndBookId(userId, bookId) ?: 0

        // 책의 총 페이지 대비 비율 계산 (0-100)
        return if (totalPages > 0) {
            ((readPages.toDouble() / totalPages) * 100).toInt().coerceIn(0, 100)
        } else {
            0
        }
    }

    /**
     * 기간에 따른 날짜 범위 계산
     */
    private fun getPeriodDateRange(period: GoalPeriod): Pair<LocalDate, LocalDate> {
        val today = LocalDate.now()

        return when (period) {
            GoalPeriod.DAILY -> {
                today to today
            }
            GoalPeriod.WEEKLY -> {
                val startOfWeek = today.minusDays(today.dayOfWeek.value.toLong() - 1)
                startOfWeek to today
            }
            GoalPeriod.MONTHLY -> {
                val startOfMonth = today.withDayOfMonth(1)
                startOfMonth to today
            }
        }
    }
}

/**
 * 진행률이 포함된 목표 데이터 클래스
 */
data class ReadingGoalWithProgress(
    val goal: ReadingGoal,
    val currentProgress: Int
)