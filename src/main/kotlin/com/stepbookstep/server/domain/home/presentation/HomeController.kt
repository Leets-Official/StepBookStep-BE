package com.stepbookstep.server.domain.home.presentation

import com.stepbookstep.server.domain.home.application.HomeQueryService
import com.stepbookstep.server.domain.home.presentation.dto.HomeResponse
import com.stepbookstep.server.global.response.ApiResponse
import com.stepbookstep.server.security.jwt.LoginUserId
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Home", description = "홈 화면 API")
@RestController
@RequestMapping("/api/v1/home")
class HomeController(
    private val homeQueryService: HomeQueryService
) {

    @Operation(
        summary = "홈 목록 조회",
        description = """
            홈 화면에 필요한 모든 데이터를 한 번에 조회합니다.

            ## 응답 구조

            ### 1. readingStatistics (독서 통계)
            - **finishedBookCount**: 누적 독서량 (완독한 책 수)
            - **cumulativeHours**: 누적 독서 시간 (시간 단위)
            - **achievementRate**: 목표 달성률 (0~100%)
            - **favoriteCategory**: 가장 좋아하는 분야 (완독 기준 상위 1개)

            ### 2. genreBooks (선호 분야 도서)
            - **type**: "category" (국가별) / "genre" (장르별)
            - **id**: 선택된 categoryId 또는 genreId
            - **name**: 분류명 (예: "한국소설", "로맨스")
            - **books**: 해당 분류의 도서 목록 (최대 20권)

            선택 로직:
            - 온보딩에서 선택한 categoryIds/genreIds 중 1개 랜덤 추출
            - "잘모르겠어요" 선택 시: DB의 전체 category/genre 중 랜덤 선택

            ### 3. recommendations (오늘의 추천 도서)
            - **lightReads**: 가벼운 책 (사용자 선호분량 ±10페이지 도서)
            - **levelUp**: 도전 레벨업 (사용자 독서등급 +15~+20 score 도서)
            - **bestseller**: 베스트셀러 도서

            ## 독서 히스토리가 없는 경우 (폴백)
            - lightReads: ~200쪽 도서
            - levelUp: level=3 도서
        """
    )
    @GetMapping
    fun getHome(
        @Parameter(hidden = true) @LoginUserId userId: Long
    ): ResponseEntity<ApiResponse<HomeResponse>> {
        val response = homeQueryService.getHome(userId)
        return ResponseEntity.ok(ApiResponse.ok(response))
    }
}
