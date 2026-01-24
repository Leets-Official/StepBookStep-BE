package com.stepbookstep.server.domain.reading.presentation

import com.stepbookstep.server.domain.reading.application.StatisticsService
import com.stepbookstep.server.domain.reading.presentation.dto.ReadingStatisticsResponse
import com.stepbookstep.server.global.response.ApiResponse
import com.stepbookstep.server.security.jwt.LoginUserId
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Statistics", description = "독서 통계 API")
@RestController
@RequestMapping("/api/v1/statistics")
class StatisticsController(
    private val statisticsService: StatisticsService
) {

    @Operation(
        summary = "전체 독서 통계 조회",
        description = "사용자의 모든 독서 통계를 조회합니다 (통계 탭용)"
    )
    @GetMapping
    fun getStatistics(
        @Parameter(hidden = true) @LoginUserId userId: Long,
        @Parameter(description = "조회 연도 (기본값: 현재 연도)")
        @RequestParam(required = false) year: Int?
    ): ResponseEntity<ApiResponse<ReadingStatisticsResponse>> {
        val targetYear = year ?: java.time.Year.now().value

        val statistics = statisticsService.getReadingStatistics(userId, targetYear)
        return ResponseEntity.ok(ApiResponse.ok(statistics))
    }
}