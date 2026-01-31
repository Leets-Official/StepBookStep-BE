package com.stepbookstep.server.domain.onboarding.presentation

import com.stepbookstep.server.domain.onboarding.application.UserOnboardingService
import com.stepbookstep.server.domain.onboarding.application.dto.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Users", description = "사용자 온보딩/프로필 관련 API")
@RestController
@RequestMapping("/api/v1/users")
class UserOnboardingController(
    private val onboardingService: UserOnboardingService
) {

    @Operation(summary = "닉네임 중복 확인", description = "온보딩 첫 화면에서 입력한 닉네임이 사용 가능한지 확인합니다. - 형식: 한글/영문/숫자만, 2자 이상 15자 이하")
    @GetMapping("/check")
    fun checkNickname(@RequestParam nickname: String): ResponseEntity<NicknameCheckResponse> {
        return ResponseEntity.ok(onboardingService.checkNickname(nickname))
    }

    @Operation(
        summary = "온보딩 정보 저장",
        description = """
    사용자의 온보딩 정보를 저장하고 가입 완료 상태로 변경합니다.

    - routineTokens: 온보딩 결과 문구를 만들기 위한 토큰 3개
    - period: "하루" 또는 "일주일"
    - amount: "10분" / "20쪽" / "20분"
    - basis: "얇은 책" 또는 "레벨 별 추천도서"
    - 단, **categoryIds+genreIds가 0개인 경우 basis는 무조건 "레벨 별 추천도서"로 응답**
    """)
    @PostMapping("/onboarding")
    fun saveOnboarding(
        @RequestAttribute("userId") userId: Long,
        @RequestBody request: OnboardingSaveRequest
    ): ResponseEntity<OnboardingSaveResponse> {
        return ResponseEntity.ok(onboardingService.saveOnboarding(userId, request))
    }
}
