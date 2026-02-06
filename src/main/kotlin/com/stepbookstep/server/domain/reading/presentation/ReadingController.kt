package com.stepbookstep.server.domain.reading.presentation

import com.stepbookstep.server.domain.book.domain.BookRepository
import com.stepbookstep.server.domain.reading.application.ReadingGoalService
import com.stepbookstep.server.domain.reading.application.ReadingLogService
import com.stepbookstep.server.domain.reading.presentation.dto.BookReadingDetailResponse
import com.stepbookstep.server.domain.reading.presentation.dto.CreateReadingLogRequest
import com.stepbookstep.server.domain.reading.presentation.dto.CreateReadingLogResponse
import com.stepbookstep.server.domain.reading.presentation.dto.ReadingGoalResponse
import com.stepbookstep.server.domain.reading.presentation.dto.RoutineItem
import com.stepbookstep.server.domain.reading.presentation.dto.RoutineListResponse
import com.stepbookstep.server.domain.reading.presentation.dto.UpsertReadingGoalRequest
import com.stepbookstep.server.global.response.ApiResponse
import com.stepbookstep.server.global.response.CustomException
import com.stepbookstep.server.global.response.ErrorCode
import com.stepbookstep.server.security.jwt.LoginUserId
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Reading", description = "독서 목표/기록 API")
@RestController
@RequestMapping("/api/v1")
class ReadingController(
    private val readingGoalService: ReadingGoalService,
    private val readingLogService: ReadingLogService,
    private val bookRepository: BookRepository
) {

    @Operation(
        summary = "루틴 목록 조회",
        description = "사용자의 모든 활성화된 독서 목표를 조회합니다. 루틴 탭에서 사용합니다."
    )
    @GetMapping("/routines")
    fun getRoutineList(
        @Parameter(hidden = true) @LoginUserId userId: Long
    ): ResponseEntity<ApiResponse<RoutineListResponse>> {
        val routines = readingGoalService.getAllActiveRoutines(userId)

        val routineItems = routines.map { routine ->
            RoutineItem.from(
                goal = routine.goal,
                bookTitle = routine.bookTitle,
                bookAuthor = routine.bookAuthor,
                bookCoverImage = routine.bookCoverImage,
                bookPublisher = routine.bookPublisher,
                bookPublishYear = routine.bookPublishYear,
                bookTotalPages = routine.bookTotalPages,
                bookStatus = routine.bookStatus,
                achievedAmount = routine.achievedAmount
            )
        }

        return ResponseEntity.ok(ApiResponse.ok(RoutineListResponse(routineItems)))
    }

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
        @Parameter(hidden = true) @LoginUserId userId: Long,
        @Valid @RequestBody request: UpsertReadingGoalRequest
    ): ResponseEntity<ApiResponse<ReadingGoalResponse?>> {
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
            currentProgress = goalWithProgress.currentProgress,
            achievedAmount = goalWithProgress.achievedAmount  // 추가!
        )

        return ResponseEntity.ok(ApiResponse.ok(response))
    }

    @Operation(summary = "책 목표 조회", description = "특정 책의 독서 목표를 조회합니다. 완독/중지 상태에서도 비활성화된 목표를 표시합니다.")
    @GetMapping("/books/{bookId}/goals")
    fun getGoal(
        @Parameter(description = "도서 ID") @PathVariable bookId: Long,
        @Parameter(hidden = true) @LoginUserId userId: Long
    ): ResponseEntity<ApiResponse<ReadingGoalResponse?>> {
        val goalWithProgress = readingGoalService.getGoalWithProgress(userId, bookId)

        val response = goalWithProgress?.let {
            ReadingGoalResponse.from(
                goal = it.goal,
                currentProgress = it.currentProgress,
                achievedAmount = it.achievedAmount  // 추가!
            )
        }

        return ResponseEntity.ok(ApiResponse.ok(response))
    }

    @Operation(
        summary = "독서 기록 생성",
        description = """
            독서 기록을 생성합니다.

            [입력 규칙]
            - recordDate: 기록 날짜 (생략 시 오늘 날짜로 자동 설정)
            - READING 상태:
              * readQuantity(읽은 페이지) 필수
              * TIME 목표인 경우 durationSeconds(읽은 시간) 필수
              * rating은 무시됨 (입력해도 저장 안 됨)
            - FINISHED 상태:
              * rating(1-5) 필수
              * readQuantity, durationSeconds는 무시됨
            - STOPPED 상태:
              * rating(1-5) 필수
              * readQuantity, durationSeconds는 무시됨
        """
    )
    @PostMapping("/books/{bookId}/reading-logs")
    fun createReadingLog(
        @Parameter(description = "도서 ID") @PathVariable bookId: Long,
        @Parameter(hidden = true) @LoginUserId userId: Long,
        @Valid @RequestBody request: CreateReadingLogRequest
    ): ResponseEntity<ApiResponse<CreateReadingLogResponse>> {
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

    @Operation(
        summary = "독서 상세 조회",
        description = """
            특정 책의 독서 상세 정보를 조회합니다.
            - 도서 상태(읽는 중/완독/중지)와 목표 정보
            - 현재 진도 (쪽수, 퍼센트)
            - 시작일/종료일
            - 각 독서 기록의 날짜, 쪽수(퍼센트), 시간
            - 완독/중지 시 별점
        """
    )
    @GetMapping("/books/{bookId}/reading-detail")
    fun getBookReadingDetail(
        @Parameter(description = "도서 ID") @PathVariable bookId: Long,
        @Parameter(hidden = true) @LoginUserId userId: Long
    ): ResponseEntity<ApiResponse<BookReadingDetailResponse>> {
        val detail = readingLogService.getBookReadingDetail(userId, bookId)
        return ResponseEntity.ok(ApiResponse.ok(detail))
    }
}