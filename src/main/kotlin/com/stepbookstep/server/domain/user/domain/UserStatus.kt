package com.stepbookstep.server.domain.user.domain

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "사용자 상태")
enum class UserStatus {
    @Schema(description = "정상 이용 중인 사용자")
    ACTIVE,

    @Schema(description = "탈퇴 처리된 사용자 (소프트 탈퇴)")
    WITHDRAWN
}