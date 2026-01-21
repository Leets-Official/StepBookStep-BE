package com.stepbookstep.server.security.jwt

import com.stepbookstep.server.global.response.CustomException
import com.stepbookstep.server.global.response.ErrorCode
import org.springframework.stereotype.Component

@Component
class AuthenticatedUserResolver(
    private val jwtProvider: JwtProvider
) {
    fun getUserId(authorizationHeader: String?): Long {
        if (authorizationHeader.isNullOrBlank()) {
            //return 1L; //테스트용 우회
            throw CustomException(ErrorCode.TOKEN_NOT_FOUND)
        }

        if (!authorizationHeader.startsWith("Bearer ")) {
            throw CustomException(ErrorCode.TOKEN_INVALID)
        }

        val token = authorizationHeader.removePrefix("Bearer ").trim()
        if (token.isBlank()) {
            throw CustomException(ErrorCode.TOKEN_NOT_FOUND)
        }

        return jwtProvider.getUserId(token)
    }
}
