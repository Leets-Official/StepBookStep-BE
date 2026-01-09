package com.stepbookstep.server.security.jwt

/**
 * JWT 토큰 타입 구분 enum
 *
 * - ACCESS  : API 인증용 Access Token
 * - REFRESH : Access Token 재발급을 위한 Refresh Token
 */

enum class TokenType {
    ACCESS, REFRESH
}