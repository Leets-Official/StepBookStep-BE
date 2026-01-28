package com.stepbookstep.server.domain.mypage.application.dto

import jakarta.validation.constraints.NotNull

data class UpdatePreferencesRequest(
    @field:NotNull
    val level: Int,

    val categoryIds: List<Long> = emptyList(),
    val genreIds: List<Long> = emptyList()
)