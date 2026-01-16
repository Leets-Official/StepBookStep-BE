package com.stepbookstep.server.domain.auth.application.dto

import jakarta.validation.constraints.NotBlank

data class LogoutRequest(
    @field:NotBlank
    val refreshToken: String
)
