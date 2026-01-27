package com.stepbookstep.server.domain.mypage.presentation

import com.stepbookstep.server.domain.mypage.application.MyProfileService
import com.stepbookstep.server.domain.mypage.application.dto.UpdateNicknameRequest
import com.stepbookstep.server.domain.mypage.application.dto.UpdatePreferencesRequest
import com.stepbookstep.server.global.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import com.stepbookstep.server.security.jwt.LoginUserId
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Settings", description = "설정 API")
@RestController
@RequestMapping("/api/v1/my/profile")
class MyProfileController(
    private val myProfileService: MyProfileService
) {

    @Operation(summary = "선호 레벨/분야 수정", description = "사용자의 선호 레벨과 선호 분야(분류/장르)를 수정합니다.")
    @PatchMapping("/preferences")
    fun updatePreferences(
        @LoginUserId userId: Long,
        @RequestBody request: UpdatePreferencesRequest
    ): ResponseEntity<ApiResponse<Void>> {
        myProfileService.updatePreferences(userId, request)
        return ResponseEntity.ok(ApiResponse.Companion.ok(null))
    }

    @Operation(summary = "닉네임 수정", description = "사용자의 닉네임을 수정합니다. 변경 시 닉네임 중복 여부를 확인합니다.")
    @PatchMapping("/nickname")
    fun updateNickname(
        @LoginUserId userId: Long,
        @RequestBody request: UpdateNicknameRequest
    ): ResponseEntity<ApiResponse<Void>> {
        myProfileService.updateNickname(userId, request)
        return ResponseEntity.ok(ApiResponse.ok(null))
    }

    @Operation(summary = "회원 탈퇴", description = "소셜 로그인 연결을 해제하고(unlink), 인증 데이터를 정리한 뒤 계정을 탈퇴 상태로 처리합니다.")
    @DeleteMapping
    fun deleteAccount(
        @LoginUserId userId: Long
    ): ResponseEntity<ApiResponse<Void>> {
        myProfileService.deleteAccount(userId)
        return ResponseEntity.ok(ApiResponse.ok(null))
    }

}