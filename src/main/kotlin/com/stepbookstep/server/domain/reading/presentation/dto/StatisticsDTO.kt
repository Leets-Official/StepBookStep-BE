package com.stepbookstep.server.domain.reading.presentation.dto

/**
 * 전체 독서 통계 응답
 */
data class ReadingStatisticsResponse(
    val bookSummary: BookSummaryDto,
    val monthlyGraph: MonthlyGraphResponse,
    val cumulativeTime: CumulativeTimeDto,
    val goalAchievement: GoalAchievementDto,
    val categoryPreference: CategoryPreferenceResponse
)

/**
 * 완독한 책 요약 정보
 */
data class BookSummaryDto(
    val finishedBookCount: Int,
    val totalWeightKg: Double
)

/**
 * 월별 독서 그래프
 */
data class MonthlyGraphResponse(
    val year: Int,
    val monthlyData: List<MonthlyDataDto>
)

data class MonthlyDataDto(
    val month: Int,  // 1-12
    val bookCount: Int,
    val isCurrentMonth: Boolean  // 현재 월인지 여부 (강조 표시용)
)

/**
 * 누적 독서 시간
 */
data class CumulativeTimeDto(
    val hours: Int,
    val minutes: Int,
    val totalMinutes: Int,
    val days: Int
)

/**
 * 누적 목표 달성 기록
 */
data class GoalAchievementDto(
    val achievementRate: Int,  // 0-100
    val maxAchievementRate: Int  // 최고 달성률 0-100
)

/**
 * 선호 분야 통계
 */
data class CategoryPreferenceResponse(
    val totalBookCount: Int,
    val categories: List<CategoryDto>
)

data class CategoryDto(
    val rank: Int,
    val categoryName: String,
    val bookCount: Int,
    val percentage: Int  // 0-100
)