package com.stepbookstep.server.domain.auth.presentation

import com.stepbookstep.server.domain.auth.application.AuthService
import com.stepbookstep.server.domain.auth.application.dto.KakaoLoginRequest
import com.stepbookstep.server.domain.auth.application.dto.KakaoLoginResponse
import com.stepbookstep.server.domain.auth.application.dto.LogoutRequest
import com.stepbookstep.server.domain.auth.application.dto.ReissueRequest
import com.stepbookstep.server.global.response.ApiResponse
import com.stepbookstep.server.security.token.RefreshTokenService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Auth", description = "인증/인가 기능을 제공하는 API")
@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
    private val refreshTokenService: RefreshTokenService
) {

    @Operation(summary = "카카오 로그인", description = "카카오 소셜 로그인으로 사용자를 식별하고, 서비스 전용 JWT(Access/Refresh)를 발급·재발급·폐기(로그아웃)합니다")
    @PostMapping("/login/kakao")
    fun kakaoLogin(
        @Valid @RequestBody request: KakaoLoginRequest
    ): ResponseEntity<ApiResponse<KakaoLoginResponse>> {
        val result = authService.kakaoLogin(request)
        return ResponseEntity.ok(ApiResponse.Companion.ok(result))
    }

    @Operation(summary = "토큰 재발급", description = "만료되었거나 재발급이 필요한 경우, 클라이언트가 보유한 refreshToken으로 새로운 accessToken을 발급합니다.")
    @PostMapping("/reissue")
    fun reissue(@Valid @RequestBody req: ReissueRequest): ResponseEntity<ApiResponse<RefreshTokenService.ReissueResult>> {
        val result = refreshTokenService.reissue(req.refreshToken)
        return ResponseEntity.ok(ApiResponse.Companion.ok(result))
    }

    @Operation(summary = "로그아웃", description = "클라이언트가 전달한 refreshToken을 서버에서 무효화(revoked)하여 더 이상 재발급에 사용할 수 없도록 처리합니다.")
    @PostMapping("/logout")
    fun logout(@RequestBody @Valid req: LogoutRequest): ApiResponse<Unit> {
        authService.logout(req)
        return ApiResponse.Companion.ok(Unit)
    }
}