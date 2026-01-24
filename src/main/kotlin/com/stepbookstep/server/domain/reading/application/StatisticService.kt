package com.stepbookstep.server.domain.reading.application

import com.stepbookstep.server.domain.book.domain.BookRepository
import com.stepbookstep.server.domain.mypage.domain.ReadStatus
import com.stepbookstep.server.domain.reading.domain.*
import com.stepbookstep.server.domain.reading.presentation.dto.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth
import kotlin.math.roundToInt

@Service
class StatisticsService(
    private val userBookRepository: UserBookRepository,
    private val readingLogRepository: ReadingLogRepository,
    private val readingGoalRepository: ReadingGoalRepository,
    private val bookRepository: BookRepository
) {

    /**
     * 전체 독서 통계 조회
     */
    @Transactional(readOnly = true)
    fun getReadingStatistics(userId: Long, year: Int): ReadingStatisticsResponse {
        return ReadingStatisticsResponse(
            bookSummary = getBookSummary(userId),
            monthlyGraph = getMonthlyGraph(userId, year),
            cumulativeTime = getCumulativeTime(userId),
            goalAchievement = getGoalAchievement(userId),
            categoryPreference = getCategoryPreference(userId)
        )
    }

    /**
     * 완독한 책 요약 정보
     * "8권 읽었어요! 총 4.6kg이에요."
     */
    @Transactional(readOnly = true)
    fun getBookSummary(userId: Long): BookSummaryDto {
        val finishedBooks = userBookRepository.findFinishedBooksByUserId(userId)
        val finishedBookCount = finishedBooks.size

        // 책의 실제 무게 합계 (단위: g → kg 변환)
        val totalWeightKg = finishedBooks.sumOf { userBook ->
            userBook.book.weight.toDouble()
        } / 1000.0  // g을 kg으로 변환

        return BookSummaryDto(
            finishedBookCount = finishedBookCount,
            totalWeightKg = String.format("%.1f", totalWeightKg).toDouble()
        )
    }

    /**
     * 월별 독서 그래프
     */
    @Transactional(readOnly = true)
    fun getMonthlyGraph(userId: Long, year: Int): MonthlyGraphResponse {
        val currentMonth = if (Year.now().value == year) {
            YearMonth.now().monthValue
        } else {
            -1  // 과거/미래 연도는 현재 월 강조 없음
        }

        val monthlyData = (1..12).map { month ->
            val startDate = LocalDate.of(year, month, 1)
            val endDate = YearMonth.of(year, month).atEndOfMonth()

            val bookCount = readingLogRepository.countFinishedBooksInMonth(
                userId = userId,
                startDate = startDate,
                endDate = endDate
            )

            MonthlyDataDto(
                month = month,
                bookCount = bookCount,
                isCurrentMonth = month == currentMonth
            )
        }

        return MonthlyGraphResponse(
            year = year,
            monthlyData = monthlyData
        )
    }

    /**
     * 누적 독서 시간
     * "92시간 5분"
     */
    @Transactional(readOnly = true)
    fun getCumulativeTime(userId: Long): CumulativeTimeDto {
        val totalSeconds = readingLogRepository.sumAllDurationByUserId(userId)
        val totalMinutes = totalSeconds / 60
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60

        return CumulativeTimeDto(
            hours = hours,
            minutes = minutes,
            totalMinutes = totalMinutes
        )
    }

    /**
     * 누적 목표 달성 기록
     * "91%"
     *
     * 달성률 계산 방법:
     * 1. 모든 목표 기간(일/주/월)에 대해 달성 여부를 확인
     * 2. 달성한 기간 수 / 전체 기간 수 * 100
     */
    @Transactional(readOnly = true)
    fun getGoalAchievement(userId: Long): GoalAchievementDto {
        // 모든 목표 조회 (활성/비활성 포함)
        val allGoals = readingGoalRepository.findAllByUserId(userId)

        if (allGoals.isEmpty()) {
            return GoalAchievementDto(
                achievementRate = 0,
                maxAchievementRate = 0
            )
        }

        var totalPeriods = 0
        var achievedPeriods = 0

        allGoals.forEach { goal ->
            // 목표가 생성된 시점부터 비활성화된 시점(또는 현재)까지의 모든 기간 계산
            val goalStartDate = goal.createdAt.toLocalDate()
            val goalEndDate = if (goal.active) {
                LocalDate.now()
            } else {
                goal.updatedAt.toLocalDate()
            }

            val periods = getPeriodsBetweenDates(goalStartDate, goalEndDate, goal.period)

            periods.forEach { (startDate, endDate) ->
                totalPeriods++

                val achieved = checkPeriodAchievement(
                    userId = userId,
                    bookId = goal.bookId,
                    startDate = startDate,
                    endDate = endDate,
                    metric = goal.metric,
                    targetAmount = goal.targetAmount
                )

                if (achieved) {
                    achievedPeriods++
                }
            }
        }

        val achievementRate = if (totalPeriods > 0) {
            ((achievedPeriods.toDouble() / totalPeriods) * 100).roundToInt()
        } else {
            0
        }

        // 최고 달성률 계산 (개별 목표별 최고 기록)
        val maxAchievementRate = calculateMaxAchievementRate(userId, allGoals)

        return GoalAchievementDto(
            achievementRate = achievementRate,
            maxAchievementRate = maxAchievementRate
        )
    }

    /**
     * 특정 기간의 목표 달성 여부 확인
     */
    private fun checkPeriodAchievement(
        userId: Long,
        bookId: Long,
        startDate: LocalDate,
        endDate: LocalDate,
        metric: GoalMetric,
        targetAmount: Int
    ): Boolean {
        val actualAmount = when (metric) {
            GoalMetric.PAGE -> {
                val baselineRecord = readingLogRepository.findLastRecordBeforeDate(
                    userId = userId,
                    bookId = bookId,
                    beforeDate = startDate
                )
                val lastRecordInPeriod = readingLogRepository.findLastRecordInDateRange(
                    userId = userId,
                    bookId = bookId,
                    startDate = startDate,
                    endDate = endDate
                )

                val baseline = baselineRecord?.readQuantity ?: 0
                val endValue = lastRecordInPeriod?.readQuantity ?: return false

                (endValue - baseline).coerceAtLeast(0)
            }
            GoalMetric.TIME -> {
                val durationSeconds = readingLogRepository.sumDurationByUserIdAndBookIdAndDateRange(
                    userId = userId,
                    bookId = bookId,
                    startDate = startDate,
                    endDate = endDate
                )
                durationSeconds / 60  // 분 단위로 변환
            }
        }

        return actualAmount >= targetAmount
    }

    /**
     * 날짜 범위를 기간 단위로 나눔
     */
    private fun getPeriodsBetweenDates(
        startDate: LocalDate,
        endDate: LocalDate,
        period: GoalPeriod
    ): List<Pair<LocalDate, LocalDate>> {
        val periods = mutableListOf<Pair<LocalDate, LocalDate>>()
        var currentDate = startDate

        while (currentDate <= endDate) {
            val periodEnd = when (period) {
                GoalPeriod.DAILY -> currentDate
                GoalPeriod.WEEKLY -> {
                    // 주의 시작은 월요일 (dayOfWeek.value == 1)
                    val daysUntilSunday = 7 - currentDate.dayOfWeek.value
                    currentDate.plusDays(daysUntilSunday.toLong()).coerceAtMost(endDate)
                }
                GoalPeriod.MONTHLY -> {
                    YearMonth.from(currentDate).atEndOfMonth().coerceAtMost(endDate)
                }
            }

            periods.add(currentDate to periodEnd)

            currentDate = when (period) {
                GoalPeriod.DAILY -> currentDate.plusDays(1)
                GoalPeriod.WEEKLY -> periodEnd.plusDays(1)
                GoalPeriod.MONTHLY -> periodEnd.plusDays(1)
            }
        }

        return periods
    }

    /**
     * 개별 목표별 최고 달성률 계산
     */
    private fun calculateMaxAchievementRate(userId: Long, goals: List<ReadingGoal>): Int {
        return goals.maxOfOrNull { goal ->
            val goalStartDate = goal.createdAt.toLocalDate()
            val goalEndDate = if (goal.active) {
                LocalDate.now()
            } else {
                goal.updatedAt.toLocalDate()
            }

            val periods = getPeriodsBetweenDates(goalStartDate, goalEndDate, goal.period)
            var achievedCount = 0

            periods.forEach { (startDate, endDate) ->
                val achieved = checkPeriodAchievement(
                    userId = userId,
                    bookId = goal.bookId,
                    startDate = startDate,
                    endDate = endDate,
                    metric = goal.metric,
                    targetAmount = goal.targetAmount
                )
                if (achieved) achievedCount++
            }

            if (periods.isNotEmpty()) {
                ((achievedCount.toDouble() / periods.size) * 100).roundToInt()
            } else {
                0
            }
        } ?: 0
    }

    /**
     * 선호 분야 통계
     * "나의 선호 분야"
     */
    @Transactional(readOnly = true)
    fun getCategoryPreference(userId: Long): CategoryPreferenceResponse {
        val finishedBooks = userBookRepository.findFinishedBooksByUserId(userId)
        val totalBookCount = finishedBooks.size

        if (totalBookCount == 0) {
            return CategoryPreferenceResponse(
                totalBookCount = 0,
                categories = emptyList()
            )
        }

        // 장르별 집계 (Book의 genre 필드 사용)
        val categoryCountMap = mutableMapOf<String, Int>()
        finishedBooks.forEach { userBook ->
            val category = userBook.book.genre.ifBlank { "미분류" }
            categoryCountMap[category] = categoryCountMap.getOrDefault(category, 0) + 1
        }


        val sortedCategories = categoryCountMap.entries
            .sortedByDescending { it.value }
            .take(3)  // 상위 3개

        val categories = sortedCategories.mapIndexed { index, entry ->
            CategoryDto(
                rank = index + 1,
                categoryName = entry.key,
                bookCount = entry.value,
                percentage = ((entry.value.toDouble() / totalBookCount) * 100).roundToInt()
            )
        }

        return CategoryPreferenceResponse(
            totalBookCount = totalBookCount,
            categories = categories
        )
    }
}