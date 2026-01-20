package com.stepbookstep.server.domain.home.presentation

import com.stepbookstep.server.domain.home.application.HomeQueryService
import com.stepbookstep.server.domain.home.presentation.dto.HomeResponse
import com.stepbookstep.server.global.response.ApiResponse
import com.stepbookstep.server.global.response.CustomException
import com.stepbookstep.server.global.response.ErrorCode
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

    @Operation(
        summary = "홈 목록 조회",
        description = """
            홈 화면에 필요한 모든 데이터를 한 번에 조회합니다.

            ## 응답 구조
            - **genreBooks**: 선택한 장르의 도서 목록 (최대 20권)
            - **recommendations**: 오늘의 추천 도서 (3가지 카테고리)
              - under200: 200쪽 미만 도서
              - levelUp: 레벨업 도전 도서
              - bestseller: 베스트셀러 도서

            ## 프론트엔드 사용 가이드
            recommendations의 3가지 리스트는 한 번에 전달되며,
            탭/버튼 클릭 시 해당 배열만 렌더링하면 됩니다.
            (추가 API 호출 불필요)
        """
    )
    @GetMapping
    fun getHome(
        // TODO: 온보딩 API 구현 후, JWT에서 사용자 ID 추출 → User 테이블의 선호 장르 조회로 변경 예정
        @Parameter(
            description = """
                장르 ID 목록 (정수 입력: 0~9)

                - 0: 추리/미스터리, 1: 라이트노벨, 2: 판타지/환상문학, 3: 역사소설, 4: 과학소설(SF), 5: 호러/공포소설, 6: 무협소설, 7: 액션/스릴러, 8: 로맨스, 9: 희곡

                [사용 방법]
                - 복수 선택 시: 선택한 장르 중 1개 랜덤 추출
                - 미입력 시: 전체 장르 중 1개 랜덤 추출

                ※ Swagger UI에 string으로 표시되지만, 실제로는 정수(0~9)를 입력해야 합니다.
                (Swagger UI에서 integer 배열의 빈 항목이 자동으로 0으로 변환되는 문제를 방지하기 위해 string으로 받고 서버에서 파싱합니다)
            """
        )
        @RequestParam(required = false) genreIds: List<String>?
    ): ResponseEntity<ApiResponse<HomeResponse>> {
        val filteredIds = genreIds?.filter { it.isNotBlank() }
        val parsedGenreIds = if (filteredIds.isNullOrEmpty()) {
            null
        } else {
            filteredIds.map { id ->
                id.toIntOrNull() ?: throw CustomException(ErrorCode.INVALID_GENRE_ID)
            }
        }
        val response = homeQueryService.getHome(parsedGenreIds)
        return ResponseEntity.ok(ApiResponse.ok(response))
    }
}
