package com.stepbookstep.server.domain.reading.presentation

import com.stepbookstep.server.domain.book.domain.BookRepository
import com.stepbookstep.server.domain.reading.application.ReadingGoalService
import com.stepbookstep.server.domain.reading.application.ReadingLogService
import com.stepbookstep.server.domain.reading.presentation.dto.CreateReadingLogRequest
import com.stepbookstep.server.domain.reading.presentation.dto.CreateReadingLogResponse
import com.stepbookstep.server.domain.reading.presentation.dto.ReadingGoalResponse
import com.stepbookstep.server.domain.reading.presentation.dto.UpsertReadingGoalRequest
import com.stepbookstep.server.global.response.ApiResponse
import com.stepbookstep.server.global.response.CustomException
import com.stepbookstep.server.global.response.ErrorCode
import com.stepbookstep.server.security.jwt.AuthenticatedUserResolver
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Reading", description = "독서 목표/기록 API")
@RestController
@RequestMapping("/api/v1")
class ReadingController(
    private val readingGoalService: ReadingGoalService,
    private val readingLogService: ReadingLogService,
    private val bookRepository: BookRepository,
    private val authenticatedUserResolver: AuthenticatedUserResolver
) {

    @Operation(
        summary = "독서 목표 생성/수정/삭제",
        description = """
            독서 목표를 생성, 수정, 삭제합니다.
            - 생성/수정: period, metric, targetAmount를 모두 포함
            - 삭제: delete=true 명시
        """
    )
    @PatchMapping("/books/{bookId}/goals")
    fun upsertOrDeleteGoal(
        @Parameter(description = "도서 ID") @PathVariable bookId: Long,
        @RequestHeader("Authorization", required = false) authorization: String?,
        @RequestBody request: UpsertReadingGoalRequest
    ): ResponseEntity<ApiResponse<ReadingGoalResponse?>> {
        val userId = authenticatedUserResolver.getUserId(authorization)

        // 삭제 요청인 경우
        if (request.delete == true) {
            readingGoalService.deleteGoal(userId, bookId)
            return ResponseEntity.ok(ApiResponse.ok(null))
        }

        // 생성/수정 요청 - 필수 필드 검증
        if (request.period == null || request.metric == null || request.targetAmount == null) {
            throw CustomException(ErrorCode.INVALID_INPUT)
        }

        // 생성/수정 요청인 경우
        val goal = readingGoalService.upsertGoal(
            userId = userId,
            bookId = bookId,
            period = request.period,
            metric = request.metric,
            targetAmount = request.targetAmount
        )

        val goalWithProgress = readingGoalService.getActiveGoalWithProgress(userId, bookId)
            ?: throw CustomException(ErrorCode.GOAL_NOT_FOUND)

        val response = ReadingGoalResponse.from(
            goal = goalWithProgress.goal,
            currentProgress = goalWithProgress.currentProgress
        )

        return ResponseEntity.ok(ApiResponse.ok(response))
    }

    @Operation(summary = "책 목표 조회", description = "특정 책의 현재 활성화된 독서 목표를 조회합니다.")
    @GetMapping("/books/{bookId}/goals")
    fun getGoal(
        @Parameter(description = "도서 ID") @PathVariable bookId: Long,
        @RequestHeader("Authorization", required = false) authorization: String?
    ): ResponseEntity<ApiResponse<ReadingGoalResponse?>> {
        val userId = authenticatedUserResolver.getUserId(authorization)
        val goalWithProgress = readingGoalService.getActiveGoalWithProgress(userId, bookId)

        val response = goalWithProgress?.let {
            ReadingGoalResponse.from(
                goal = it.goal,
                currentProgress = it.currentProgress
            )
        }

        return ResponseEntity.ok(ApiResponse.ok(response))
    }

    @Operation(
        summary = "독서 기록 생성",
        description = """
            독서 기록을 생성합니다.
            
            [입력 규칙]
            - READING 상태:
              * readQuantity(읽은 페이지) 필수
              * TIME 목표인 경우 durationSeconds(읽은 시간) 필수
              * rating은 무시됨 (입력해도 저장 안 됨)
            - FINISHED 상태:
              * rating(1-5) 필수
            - STOPPED 상태:
              * rating(1-5) 필수
        """
    )
    @PostMapping("/books/{bookId}/reading-logs")
    fun createReadingLog(
        @Parameter(description = "도서 ID") @PathVariable bookId: Long,
        @RequestHeader("Authorization", required = false) authorization: String?,
        @RequestBody request: CreateReadingLogRequest
    ): ResponseEntity<ApiResponse<CreateReadingLogResponse>> {
        val userId = authenticatedUserResolver.getUserId(authorization)
        val log = readingLogService.createLog(
            userId = userId,
            bookId = bookId,
            bookStatus = request.bookStatus,
            recordDate = request.recordDate,
            readQuantity = request.readQuantity,
            durationSeconds = request.durationSeconds,
            rating = request.rating
        )
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(CreateReadingLogResponse(log.id)))
    }
}