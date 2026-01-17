package com.stepbookstep.server.domain.home.presentation

import com.stepbookstep.server.domain.home.application.HomeQueryService
import com.stepbookstep.server.domain.home.presentation.dto.HomeResponse
import com.stepbookstep.server.global.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Home", description = "홈 화면 API")
@RestController
@RequestMapping("/api/v1/home")
class HomeController(
    private val homeQueryService: HomeQueryService
) {

    @Operation(summary = "홈 목록 조회", description = "홈 화면에 필요한 모든 데이터를 조회합니다.")
    @GetMapping
    fun getHome(
        // TODO: 온보딩 API 구현 후, JWT에서 사용자 ID 추출 → User 테이블의 선호 장르 조회로 변경 예정
        @Parameter(
            description = "장르 ID (0: 추리/미스터리, 1: 라이트 노벨, 2: 판타지/환상문학, 3: 역사소설, 4: 과학소설(SF), 5: 호러/공포소설, 6: 무협소설, 7: 액션/스릴러, 8: 로맨스, 9: 시, 10: 희곡)"
        )
        @RequestParam(required = false) genreId: Int?
    ): ResponseEntity<ApiResponse<HomeResponse>> {
        val response = homeQueryService.getHome(genreId)
        return ResponseEntity.ok(ApiResponse.ok(response))
    }
}
