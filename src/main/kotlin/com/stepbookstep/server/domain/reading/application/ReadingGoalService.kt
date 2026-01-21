package com.stepbookstep.server.domain.reading.application

import com.stepbookstep.server.domain.book.domain.BookRepository
import com.stepbookstep.server.domain.mypage.domain.ReadStatus
import com.stepbookstep.server.domain.reading.domain.GoalMetric
import com.stepbookstep.server.domain.reading.domain.GoalPeriod
import com.stepbookstep.server.domain.reading.domain.ReadingGoal
import com.stepbookstep.server.domain.reading.domain.ReadingGoalRepository
import com.stepbookstep.server.domain.reading.domain.ReadingLogRepository
import com.stepbookstep.server.domain.reading.domain.UserBook
import com.stepbookstep.server.domain.reading.domain.UserBookRepository
import com.stepbookstep.server.global.response.CustomException
import com.stepbookstep.server.global.response.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.OffsetDateTime
import kotlin.jvm.optionals.getOrNull

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
        // targetAmount 검증
        if (targetAmount <= 0) {
            throw CustomException(ErrorCode.TARGET_AMOUNT_INVALID)
        }

        if (!bookRepository.existsById(bookId)) {
            throw CustomException(ErrorCode.BOOK_NOT_FOUND)
        }

        val book = bookRepository.findById(bookId)
            .orElseThrow { CustomException(ErrorCode.BOOK_NOT_FOUND) }

        // UserBook 생성 또는 상태 업데이트
        val userBook = userBookRepository.findByUserIdAndBookId(userId, bookId)
            ?: UserBook(
                userId = userId,
                book = book,
                status = ReadStatus.READING
            )

        if (userBook.status != ReadStatus.READING) {
            userBook.status = ReadStatus.READING
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

        val currentProgress = calculateCurrentProgress(userId, bookId,  book.itemPage)

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

        val currentProgress = calculateCurrentProgress(userId, bookId,  book.itemPage)

        return ReadingGoalWithProgress(
            goal = goal,
            currentProgress = currentProgress
        )
    }

    /**
     * 사용자의 모든 활성 목표 조회 (루틴 목록)
     * 최근 생성순으로 정렬
     */
    @Transactional(readOnly = true)
    fun getAllActiveRoutines(userId: Long): List<RoutineWithDetails> {
        val activeGoals = readingGoalRepository.findAllByUserIdAndActiveTrueOrderByCreatedAtDesc(userId)

        return activeGoals.mapNotNull { goal ->
            val book = bookRepository.findById(goal.bookId).getOrNull() ?: return@mapNotNull null
            val userBook = userBookRepository.findByUserIdAndBookId(userId, goal.bookId) ?: return@mapNotNull null

            val currentProgress = calculateCurrentProgress(
                userId = userId,
                bookId = goal.bookId,
                totalPages = book.itemPage
            )

            // 현재 기간에 달성한 양 계산
            val achievedAmount = calculateAchievedAmount(
                userId = userId,
                bookId = goal.bookId,
                period = goal.period,
                metric = goal.metric
            )

            RoutineWithDetails(
                goal = goal,
                bookTitle = book.title,
                bookAuthor = book.author,
                bookCoverImage = book.coverUrl,
                bookPublisher = book.publisher,
                bookPublishYear = book.pubYear,
                bookTotalPages = book.itemPage,
                bookStatus = userBook.status,
                currentProgress = currentProgress,
                achievedAmount = achievedAmount
            )
        }
    }

    /**
     * 현재 기간에 달성한 양 계산
     * - PAGE: baseline(기간 시작 전)과 마지막 기록 차이
     * - TIME: 기간 내 총 독서 시간 합계
     */
    private fun calculateAchievedAmount(
        userId: Long,
        bookId: Long,
        period: GoalPeriod,
        metric: GoalMetric
    ): Int {
        val (startDate, endDate) = getPeriodDateRange(period)

        return when (metric) {
            GoalMetric.PAGE -> {
                // 1) 기간 시작 전 마지막 기록 = baseline
                val baselineRecord = readingLogRepository.findLastRecordBeforeDate(
                    userId = userId,
                    bookId = bookId,
                    beforeDate = startDate
                )

                // 2) 기간 내 마지막 기록 = endValue
                val lastRecordInPeriod = readingLogRepository.findLastRecordInDateRange(
                    userId = userId,
                    bookId = bookId,
                    startDate = startDate,
                    endDate = endDate
                )

                val baseline = baselineRecord?.readQuantity ?: 0
                val endValue = lastRecordInPeriod?.readQuantity ?: return 0

                (endValue - baseline).coerceAtLeast(0)
            }

            GoalMetric.TIME -> {
                val durationSeconds = readingLogRepository.sumDurationByUserIdAndBookIdAndDateRange(
                    userId = userId,
                    bookId = bookId,
                    startDate = startDate,
                    endDate = endDate
                )
                durationSeconds / 60
            }
        }
    }


    /**
     * 현재 진행률 계산 (책 전체 대비 읽은 비율 0-100)
     */
    private fun calculateCurrentProgress(
        userId: Long,
        bookId: Long,
        totalPages: Int
    ): Int {
        val latest = readingLogRepository.findLatestRecordByUserIdAndBookId(userId, bookId)
        val currentPage = latest?.readQuantity ?: 0

        return if (totalPages > 0) {
            ((currentPage.toDouble() / totalPages) * 100).toInt().coerceIn(0, 100)
        } else 0
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

/**
 * 루틴 목록용 상세 정보가 포함된 목표 데이터 클래스
 */
data class RoutineWithDetails(
    val goal: ReadingGoal,
    val bookTitle: String,
    val bookAuthor: String,
    val bookCoverImage: String?,
    val bookPublisher: String?,
    val bookPublishYear: Int?,
    val bookTotalPages: Int,
    val bookStatus: UserBookStatus,
    val currentProgress: Int,
    val achievedAmount: Int
)