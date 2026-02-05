package com.stepbookstep.server.domain.auth.application.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class KakaoTokenResponse(
    @JsonProperty("access_token")
    val accessToken: String,

    @JsonProperty("refresh_token")
    val refreshToken: String,

    @JsonProperty("expires_in")
    val expiresIn: Int
)
