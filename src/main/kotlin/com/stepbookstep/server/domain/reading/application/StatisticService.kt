package com.stepbookstep.server.domain.reading.application

import com.stepbookstep.server.domain.book.domain.BookRepository
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
     */
    @Transactional(readOnly = true)
    fun getBookSummary(userId: Long): BookSummaryDto {
        val finishedBooks = userBookRepository.findFinishedBooksByUserId(userId)
        val finishedBookCount = finishedBooks.size

        val totalWeightKg = finishedBooks.sumOf { userBook ->
            (userBook.book.weight ?: 0).toDouble()
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
            -1
        }

        val monthlyData = readingLogRepository.countFinishedBooksGroupedByMonth(userId, year)
            .associate {
                val month = (it[0] as Number).toInt()
                val count = (it[1] as Number).toInt()
                month to count
            }

        val allMonthsData = (1..12).map { month ->
            MonthlyDataDto(
                month = month,
                bookCount = monthlyData[month] ?: 0,
                isCurrentMonth = month == currentMonth
            )
        }

        return MonthlyGraphResponse(
            year = year,
            monthlyData = allMonthsData
        )
    }

    /**
     * 누적 독서 시간
     */
    @Transactional(readOnly = true)
    fun getCumulativeTime(userId: Long): CumulativeTimeDto {
        val totalSeconds = readingLogRepository.sumAllDurationByUserId(userId)
            ?.toLong() ?: 0L

        val totalMinutes = (totalSeconds / 60).toInt()
        val hours = (totalMinutes / 60)
        val minutes = (totalMinutes % 60)

        return CumulativeTimeDto(
            hours = hours,
            minutes = minutes,
            totalMinutes = totalMinutes
        )
    }

    /**
     * 누적 목표 달성 기록
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

        // 모든 목표의 기록을 한 번에 조회 (N+1 쿼리 방지)
        val bookIds = allGoals.map { it.bookId }.distinct()
        val earliestGoalDate = allGoals.minOf { it.createdAt.toLocalDate() }

        val allLogs = readingLogRepository.findAllByBooksInDateRange(
            userId = userId,
            bookIds = bookIds,
            startDate = earliestGoalDate
        ).groupBy { it.bookId }

        var totalPeriods = 0
        var achievedPeriods = 0

        allGoals.forEach { goal ->
            val goalStartDate = goal.createdAt.toLocalDate()
            val goalEndDate = if (goal.active) {
                LocalDate.now()
            } else {
                goal.updatedAt.toLocalDate()
            }

            val periods = getPeriodsBetweenDates(goalStartDate, goalEndDate, goal.period)
            val bookLogs = allLogs[goal.bookId] ?: emptyList()

            periods.forEach { (startDate, endDate) ->
                totalPeriods++

                val achieved = checkPeriodAchievementInMemory(
                    logs = bookLogs,
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

        val maxAchievementRate = calculateMaxAchievementRateInMemory(userId, allGoals, allLogs)

        return GoalAchievementDto(
            achievementRate = achievementRate,
            maxAchievementRate = maxAchievementRate
        )
    }

    /**
     * 기간 달성 여부 확인
     */
    private fun checkPeriodAchievementInMemory(
        logs: List<ReadingLog>,
        startDate: LocalDate,
        endDate: LocalDate,
        metric: GoalMetric,
        targetAmount: Int
    ): Boolean {
        val actualAmount = when (metric) {
            GoalMetric.PAGE -> {
                // 기간 시작 전 마지막 기록
                val baselineRecord = logs
                    .filter { it.recordDate < startDate && it.readQuantity != null }
                    .maxByOrNull { it.recordDate }

                // 기간 내 마지막 기록
                val lastRecordInPeriod = logs
                    .filter { it.recordDate in startDate..endDate && it.readQuantity != null }
                    .maxByOrNull { it.recordDate }

                val baseline = baselineRecord?.readQuantity ?: 0
                val endValue = lastRecordInPeriod?.readQuantity ?: return false

                (endValue - baseline).coerceAtLeast(0)
            }
            GoalMetric.TIME -> {
                logs
                    .filter { it.recordDate in startDate..endDate && it.durationSeconds != null }
                    .sumOf { it.durationSeconds ?: 0 } / 60
            }
        }

        return actualAmount >= targetAmount
    }

    /**
     * 날짜 범위를 기간 단위로 나눔
     * 목표 생성일 기준으로 기간 분할
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
                    // 생성일 기준 7일 단위
                    currentDate.plusDays(6).coerceAtMost(endDate)
                }
                GoalPeriod.MONTHLY -> {
                    // 생성일 기준 1개월 단위
                    currentDate.plusMonths(1).minusDays(1).coerceAtMost(endDate)
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
    private fun calculateMaxAchievementRateInMemory(
        userId: Long,
        goals: List<ReadingGoal>,
        allLogs: Map<Long, List<ReadingLog>>
    ): Int {
        return goals.maxOfOrNull { goal ->
            val goalStartDate = goal.createdAt.toLocalDate()
            val goalEndDate = if (goal.active) {
                LocalDate.now()
            } else {
                goal.updatedAt.toLocalDate()
            }

            val periods = getPeriodsBetweenDates(goalStartDate, goalEndDate, goal.period)
            val bookLogs = allLogs[goal.bookId] ?: emptyList()

            if (periods.isEmpty()) return@maxOfOrNull 0

            val achievedCount = periods.count { (startDate, endDate) ->
                checkPeriodAchievementInMemory(
                    logs = bookLogs,
                    startDate = startDate,
                    endDate = endDate,
                    metric = goal.metric,
                    targetAmount = goal.targetAmount
                )
            }

            ((achievedCount.toDouble() / periods.size) * 100).roundToInt()
        } ?: 0
    }

    /**
     * 선호 분야 통계
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

        // 내림차순 정렬 후 상위 3개만 선택
        val sortedCategories = categoryCountMap.entries
            .sortedByDescending { it.value }
            .take(3)  // 상위 3개만

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