package com.stepbookstep.server.domain.auth.application

import com.stepbookstep.server.domain.user.application.UserService
import com.stepbookstep.server.domain.auth.application.dto.KakaoLoginRequest
import com.stepbookstep.server.domain.auth.application.dto.KakaoLoginResponse
import com.stepbookstep.server.domain.auth.application.dto.LogoutRequest
import com.stepbookstep.server.external.kakao.KakaoApiClient
import com.stepbookstep.server.external.kakao.KakaoUserMeResponse
import com.stepbookstep.server.global.response.CustomException
import com.stepbookstep.server.global.response.ErrorCode
import com.stepbookstep.server.security.jwt.JwtProvider
import com.stepbookstep.server.security.jwt.KakaoProperties
import com.stepbookstep.server.security.jwt.TokenType
import com.stepbookstep.server.security.token.RefreshTokenService
import org.springframework.stereotype.Service

/**
 * 카카오 로그인 비즈니스 로직 (로그인/재발급/로그아웃)을 담당
 * - 외부 API 호출과 DB 트랜잭션을 분리하여 성능을 최적화
 */
@Service
class AuthService(
    private val kakaoApiClient: KakaoApiClient,
    private val userService: UserService,
    private val jwtProvider: JwtProvider,
    private val refreshTokenService: RefreshTokenService,
    private val kakaoProperties: KakaoProperties,
) {

    /**
     * 카카오 로그인 전체 흐름 제어 (트랜잭션 없음)
     */
    fun kakaoLogin(request: KakaoLoginRequest): KakaoLoginResponse {
        val kakaoAccessToken = request.socialToken.trim()
        if (kakaoAccessToken.isBlank()) {
            throw CustomException(ErrorCode.INVALID_INPUT)
        }

        // 1) 카카오 사용자 정보 조회 (외부 API 호출 - Network I/O)
        val kakaoMe: KakaoUserMeResponse = kakaoApiClient.getMe(kakaoAccessToken)
        val providerUserId = kakaoMe.id.toString()
        val nicknameFromKakao = kakaoMe.nickname ?: "사용자"
        val email = kakaoMe.email
            ?: throw CustomException(ErrorCode.EMAIL_REQUIRED)

        // 2) 유저 조회/생성 (DB Transaction - UserService 내부에서 처리)
        val (user, isNewUser) =
            userService.getOrCreateKakaoUser(
                providerUserId = providerUserId,
                nickname = nicknameFromKakao,
                email = email
            )

        // 3) JWT 발급 (CPU 연산)
        val accessToken = jwtProvider.createToken(user.id, TokenType.ACCESS)
        val refreshToken = jwtProvider.createToken(user.id, TokenType.REFRESH)

        // 4) Refresh Token 저장 (DB Transaction - RefreshTokenService 내부에서 처리)
        refreshTokenService.save(userId = user.id, refreshToken = refreshToken)

        return KakaoLoginResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            isNewUser = isNewUser,
            nickname = user.nickname,
            email = user.email
        )
    }

    fun kakaoLoginWithCode(code: String): KakaoLoginResponse {
        if (code.isBlank()) {
            throw CustomException(ErrorCode.INVALID_INPUT)
        }

        // 1) 인가 코드 → 카카오 Access Token 교환
        val tokenResponse = kakaoApiClient.getToken(
            code = code,
            redirectUri = kakaoProperties.redirectUri
        )

        val kakaoAccessToken = tokenResponse.accessToken

        // 2) 아래는 기존 로직 재사용
        val kakaoMe = kakaoApiClient.getMe(kakaoAccessToken)
        val providerUserId = kakaoMe.id.toString()
        val nicknameFromKakao = kakaoMe.nickname ?: "사용자"
        val email = kakaoMe.email
            ?: throw CustomException(ErrorCode.EMAIL_REQUIRED)

        val (user, isNewUser) =
            userService.getOrCreateKakaoUser(
                providerUserId = providerUserId,
                nickname = nicknameFromKakao,
                email = email
            )

        val accessToken = jwtProvider.createToken(user.id, TokenType.ACCESS)
        val refreshToken = jwtProvider.createToken(user.id, TokenType.REFRESH)

        refreshTokenService.save(userId = user.id, refreshToken = refreshToken)

        return KakaoLoginResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            isNewUser = isNewUser,
            nickname = user.nickname,
            email = user.email
        )
    }

    fun logout(req: LogoutRequest) {
        refreshTokenService.logout(req.refreshToken)
    }
}