package com.stepbookstep.server.domain.mypage.presentation

import com.stepbookstep.server.domain.mypage.application.ReadingStatusService
import com.stepbookstep.server.domain.mypage.application.dto.UpdateReadStatusRequest
import com.stepbookstep.server.global.response.ApiResponse
import com.stepbookstep.server.security.jwt.LoginUserId
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "MyPage", description = "마이페이지(읽기 상태 관리) API")
@RestController
@RequestMapping("/api/v1/my/books")
class ReadingStatusController(
    private val readingStatusService: ReadingStatusService
) {

    @Operation(
        summary = "읽기 상태 변경",
        description = """
            내 서재 항목(userBookId)의 읽기 상태를 변경합니다.
            
            상태: READING / STOPPED / FINISHED
        """
    )
    @PatchMapping("/{userBookId}/status")
    fun updateReadStatus(
        @Parameter(description = "내 서재 항목 ID(userBookId)", example = "10")
        @PathVariable userBookId: Long,

        @Parameter(hidden = true)
        @LoginUserId userId: Long,

        @RequestBody request: UpdateReadStatusRequest
    ): ResponseEntity<ApiResponse<Void>> {

        readingStatusService.updateStatus(
            userId = userId,
            userBookId = userBookId,
            newStatus = request.status,
            rating = request.rating
        )

        return ResponseEntity.ok(ApiResponse.ok())
    }
}