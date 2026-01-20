package com.stepbookstep.server.security.jwt

import com.stepbookstep.server.global.response.CustomException
import com.stepbookstep.server.global.response.ErrorCode
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

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {

        val authHeader = request.getHeader("Authorization")
            ?: throw CustomException(ErrorCode.TOKEN_NOT_FOUND)

        if (!authHeader.startsWith("Bearer ")) {
            throw CustomException(ErrorCode.TOKEN_UNSUPPORTED)
        }

        val token = authHeader.substring(7)

        // 만료 여부 체크
        if (jwtProvider.isExpired(token)) {
            throw CustomException(ErrorCode.TOKEN_EXPIRED)
        }

        // 토큰에서 userId 추출 (위조 / 파싱 실패 대비)
        val userId = try {
            jwtProvider.getUserId(token)
        } catch (e: Exception) {
            throw CustomException(ErrorCode.TOKEN_INVALID)
        }

        request.setAttribute("userId", userId)
        return true
    }
}


