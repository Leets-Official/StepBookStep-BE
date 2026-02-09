package com.stepbookstep.server.domain.user.domain

enum class SignupType {
    NEW,          // 완전 신규
    REJOIN,       // 탈퇴 후 재가입
    EXISTING      // 기존 유저
}
