package com.stepbookstep.server.auth

import com.stepbookstep.server.auth.dto.KakaoLoginRequest
import com.stepbookstep.server.auth.dto.KakaoLoginResponse
import com.stepbookstep.server.auth.dto.LogoutRequest
import com.stepbookstep.server.external.kakao.KakaoApiClient
import com.stepbookstep.server.external.kakao.KakaoUserMeResponse
import com.stepbookstep.server.global.response.CustomException
import com.stepbookstep.server.global.response.ErrorCode
import com.stepbookstep.server.security.jwt.JwtProvider
import com.stepbookstep.server.security.jwt.TokenType
import com.stepbookstep.server.security.token.RefreshTokenService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 카카오 로그인 비즈니스 로직 (로그인/재발급/로그아웃)을 담당
 */
@Service
class AuthService(
    private val kakaoApiClient: KakaoApiClient,
    private val userService: UserService,
    private val jwtProvider: JwtProvider,
    private val refreshTokenService: RefreshTokenService,
) {

    @Transactional
    fun kakaoLogin(request: KakaoLoginRequest): KakaoLoginResponse {
        val kakaoAccessToken = request.socialToken.trim()
        if (kakaoAccessToken.isBlank()) {
            throw CustomException(ErrorCode.INVALID_INPUT)
        }

        // 1) 카카오 사용자 정보 조회
        val kakaoMe: KakaoUserMeResponse = kakaoApiClient.getMe(kakaoAccessToken)
        val providerUserId = kakaoMe.id.toString()
        val nicknameFromKakao = kakaoMe.nickname

        // 2) 유저 조회/생성 + 신규 여부 판단
        val existingUser = userService.findByKakaoProviderUserId(providerUserId)
        val user = existingUser ?: userService.createKakaoUser(
            providerUserId = providerUserId,
            nickname = nicknameFromKakao ?: "사용자"
        )
        val isNewUser = (existingUser == null)

        // 3) JWT 발급
        val accessToken = jwtProvider.createToken(user.id, TokenType.ACCESS)
        val refreshToken = jwtProvider.createToken(user.id, TokenType.REFRESH)

        // 4) refresh token 저장 (SHA-256 해시는 RefreshTokenService 내부에서 처리)
        refreshTokenService.save(userId = user.id, refreshToken = refreshToken)

        return KakaoLoginResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            isNewUser = isNewUser,
            nickname = user.nickname
        )
    }

    fun logout(req: LogoutRequest) {
        refreshTokenService.logout(req.refreshToken)
    }
}
