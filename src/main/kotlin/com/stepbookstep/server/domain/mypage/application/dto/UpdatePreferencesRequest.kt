package com.stepbookstep.server.domain.mypage.application.dto

data class UpdatePreferencesRequest(
    val level: Int,
    val preferences: List<CategoryGenrePreferenceRequest>
)

data class CategoryGenrePreferenceRequest(
    val categoryId: Long,
    val genreId: Long
)