package com.stepbookstep.server.domain.reading.presentation

import com.stepbookstep.server.domain.reading.application.StatisticsService
import com.stepbookstep.server.domain.reading.presentation.dto.*
import com.stepbookstep.server.global.response.ApiResponse
import com.stepbookstep.server.security.jwt.AuthenticatedUserResolver
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Statistics", description = "독서 통계 API")
@RestController
@RequestMapping("/api/v1/statistics")
class StatisticsController(
    private val statisticsService: StatisticsService,
    private val authenticatedUserResolver: AuthenticatedUserResolver
) {

    @Operation(
        summary = "전체 독서 통계 조회",
        description = "사용자의 모든 독서 통계를 조회합니다 (통계 탭용)"
    )
    @GetMapping
    fun getStatistics(
        @RequestHeader("Authorization", required = false) authorization: String?,
        @Parameter(description = "조회 연도 (기본값: 현재 연도)")
        @RequestParam(required = false) year: Int?
    ): ResponseEntity<ApiResponse<ReadingStatisticsResponse>> {
        val userId = authenticatedUserResolver.getUserId(authorization)
        val targetYear = year ?: java.time.Year.now().value

        val statistics = statisticsService.getReadingStatistics(userId, targetYear)
        return ResponseEntity.ok(ApiResponse.ok(statistics))
    }

    @Operation(
        summary = "월별 독서 그래프 조회",
        description = "특정 연도의 월별 독서 기록을 조회합니다"
    )
    @GetMapping("/monthly-graph")
    fun getMonthlyGraph(
        @RequestHeader("Authorization", required = false) authorization: String?,
        @Parameter(description = "조회 연도")
        @RequestParam year: Int
    ): ResponseEntity<ApiResponse<MonthlyGraphResponse>> {
        val userId = authenticatedUserResolver.getUserId(authorization)
        val monthlyData = statisticsService.getMonthlyGraph(userId, year)
        return ResponseEntity.ok(ApiResponse.ok(monthlyData))
    }

    @Operation(
        summary = "선호 분야 통계 조회",
        description = "사용자의 독서 선호 분야를 조회합니다"
    )
    @GetMapping("/category-preference")
    fun getCategoryPreference(
        @RequestHeader("Authorization", required = false) authorization: String?
    ): ResponseEntity<ApiResponse<CategoryPreferenceResponse>> {
        val userId = authenticatedUserResolver.getUserId(authorization)
        val preference = statisticsService.getCategoryPreference(userId)
        return ResponseEntity.ok(ApiResponse.ok(preference))
    }
}