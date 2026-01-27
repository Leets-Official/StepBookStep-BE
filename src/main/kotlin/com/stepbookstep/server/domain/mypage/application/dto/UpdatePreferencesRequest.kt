package com.stepbookstep.server.domain.mypage.application.dto

data class UpdatePreferencesRequest(
    val level: Int,
    val categoryIds: List<Int>
)