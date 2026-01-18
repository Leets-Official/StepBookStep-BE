package com.stepbookstep.server.security.jwt

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

/**
 * HTTP 요청마다 Authorization 헤더의 JWT를 검사하고,
 * 유효한 경우 토큰에서 userId를 추출해 request attribute로 저장하는 인증 인터셉터
 */
@Component
class AuthenticationInterceptor(
    private val jwtProvider: JwtProvider
) : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        // 1. 헤더에서 Authorization: Bearer {토큰} 추출
        val authHeader = request.getHeader("Authorization")

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            val token = authHeader.substring(7)

            // 2. 토큰이 유효하면 userId를 추출해서 request에 세팅
            if (!jwtProvider.isExpired(token)) {
                val userId = jwtProvider.getUserId(token)
                request.setAttribute("userId", userId)
            }
        }
        return true
    }
}