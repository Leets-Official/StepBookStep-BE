package com.stepbookstep.server.auth.dto

data class ReissueRequest(
    @field:jakarta.validation.constraints.NotBlank
    val refreshToken: String
)
