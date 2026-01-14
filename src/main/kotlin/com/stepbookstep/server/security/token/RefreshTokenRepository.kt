package com.stepbookstep.server.security.token

import org.springframework.data.jpa.repository.JpaRepository

interface RefreshTokenRepository : JpaRepository<RefreshTokenEntity, Long> {
    // 특정 유저의 모든 리프레시 토큰 삭제
    fun deleteByUserId(userId: Long)

    // 토큰 해시로 조회 (로그아웃이나 재발급 시 사용)
    fun findByTokenHash(tokenHash: String): RefreshTokenEntity?
}