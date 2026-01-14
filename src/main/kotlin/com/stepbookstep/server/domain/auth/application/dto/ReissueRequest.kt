package com.stepbookstep.server.domain.auth.application.dto

import jakarta.validation.constraints.NotBlank

data class ReissueRequest(
    @field:NotBlank
    val refreshToken: String
)
