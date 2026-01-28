package com.stepbookstep.server.domain.mypage.application.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull

data class UpdatePreferencesRequest(
    @field:NotNull
    val level: Int,

    @Schema(
        description = """
            선호 분류 ID 목록 (국가/권역 기준).
            - 예) 한국소설, 영미소설
            - 복수 선택 가능
            - 선택하지 않을 경우 빈 배열([]) 전달
        """,
        example = "[1, 2]"
    )
    val categoryIds: List<Long> = emptyList(),

    @Schema(
        description = """
            선호 장르 ID 목록.
            - 예) 로맨스, 판타지/환상문학
            - 복수 선택 가능
            - 선택하지 않을 경우 빈 배열([]) 전달
        """,
        example = "[10, 11, 12]"
    )
    val genreIds: List<Long> = emptyList()
)