package com.stepbookstep.server.security.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.Date

/**
 * JWT 생성 및 검증을 담당하는 Provider 클래스
 *
 * 역할:
 * - Access Token / Refresh Token 생성
 * - JWT 서명 및 만료 시간 설정
 * - 토큰 파싱을 통한 사용자 식별(userId) 및 토큰 타입 확인
 *
 */

@Component
class JwtProvider(
    private val props: JwtProperties
) {
    private val signingKey = Keys.hmacShaKeyFor(props.key.toByteArray(StandardCharsets.UTF_8))

    /**
     * 사용자 ID를 기반으로 Access Token 생성
     */
    fun createAccessToken(userId: Long): String =
        createToken(userId, TokenType.ACCESS, props.access.expiration)

    /**
     * 사용자 ID를 기반으로 Refresh Token 생성
     */
    fun createRefreshToken(userId: Long): String =
        createToken(userId, TokenType.REFRESH, props.refresh.expiration)

    private fun createToken(userId: Long, type: TokenType, expiresInMs: Long): String {
        val now = Date()
        val expiry = Date(now.time + expiresInMs)

        return Jwts.builder()
            .setSubject(userId.toString())
            .claim(CLAIM_TYPE, type.name) // ACCESS / REFRESH
            .setIssuedAt(now)
            .setExpiration(expiry)
            .signWith(signingKey)
            .compact()
    }

    /**
     * JWT 유효성 검증
     * - 서명 검증
     * - 만료 여부 검증
     */
    fun validate(token: String): Boolean =
        try {
            parseClaims(token)
            true
        } catch (e: Exception) {
            false
        }

    fun parseClaims(token: String): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(signingKey)
            .build()
            .parseClaimsJws(token)
            .body
    }

    /**
     * JWT에서 사용자 ID(subject) 추출
     */
    fun getUserId(token: String): Long =
        parseClaims(token).subject.toLong()

    /**
     * JWT의 토큰 타입(ACCESS / REFRESH) 추출
     */
    fun getTokenType(token: String): TokenType =
        TokenType.valueOf(parseClaims(token)[CLAIM_TYPE].toString())

    companion object {
        private const val CLAIM_TYPE = "typ"
    }
}
