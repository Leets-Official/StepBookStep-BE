package com.stepbookstep.server.domain.reading.presentation

import com.stepbookstep.server.domain.book.domain.BookRepository
import com.stepbookstep.server.domain.reading.application.ReadingGoalService
import com.stepbookstep.server.domain.reading.application.ReadingLogService
import com.stepbookstep.server.domain.reading.presentation.dto.BookReadingDetailResponse
import com.stepbookstep.server.domain.reading.presentation.dto.CreateReadingGoalRequest
import com.stepbookstep.server.domain.reading.presentation.dto.CreateReadingLogRequest
import com.stepbookstep.server.domain.reading.presentation.dto.CreateReadingLogResponse
import com.stepbookstep.server.domain.reading.presentation.dto.ReadingGoalResponse
import com.stepbookstep.server.domain.reading.presentation.dto.RoutineItem
import com.stepbookstep.server.domain.reading.presentation.dto.RoutineListResponse
import com.stepbookstep.server.domain.reading.presentation.dto.UpdateReadingGoalRequest
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
import org.springframework.web.bind.annotation.DeleteMapping
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
        summary = "독서 목표 생성",
        description = """
            새로운 독서 목표를 생성합니다.
            - period, metric, targetAmount 모두 필수
            - 이미 활성 목표가 있으면 해당 목표를 수정합니다.
        """
    )
    @PostMapping("/books/{bookId}/goals")
    fun createGoal(
        @Parameter(description = "도서 ID") @PathVariable bookId: Long,
        @Parameter(hidden = true) @LoginUserId userId: Long,
        @Valid @RequestBody request: CreateReadingGoalRequest
    ): ResponseEntity<ApiResponse<ReadingGoalResponse>> {
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
            achievedAmount = goalWithProgress.achievedAmount
        )

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(response))
    }

    @Operation(
        summary = "독서 목표 수정",
        description = """
            기존 독서 목표를 수정합니다.
            - period, metric, targetAmount 중 변경할 필드만 전달 가능
            - 활성 목표가 없으면 404
        """
    )
    @PatchMapping("/books/{bookId}/goals")
    fun updateGoal(
        @Parameter(description = "도서 ID") @PathVariable bookId: Long,
        @Parameter(hidden = true) @LoginUserId userId: Long,
        @Valid @RequestBody request: UpdateReadingGoalRequest
    ): ResponseEntity<ApiResponse<ReadingGoalResponse>> {
        // 기존 활성 목표가 있어야 수정 가능
        val existing = readingGoalService.getActiveGoalWithProgress(userId, bookId)
            ?: throw CustomException(ErrorCode.GOAL_NOT_FOUND)

        val goal = readingGoalService.upsertGoal(
            userId = userId,
            bookId = bookId,
            period = request.period ?: existing.goal.period,
            metric = request.metric ?: existing.goal.metric,
            targetAmount = request.targetAmount ?: existing.goal.targetAmount
        )

        val goalWithProgress = readingGoalService.getActiveGoalWithProgress(userId, bookId)
            ?: throw CustomException(ErrorCode.GOAL_NOT_FOUND)

        val response = ReadingGoalResponse.from(
            goal = goalWithProgress.goal,
            currentProgress = goalWithProgress.currentProgress,
            achievedAmount = goalWithProgress.achievedAmount
        )

        return ResponseEntity.ok(ApiResponse.ok(response))
    }

    @Operation(
        summary = "독서 목표 삭제",
        description = "활성 독서 목표를 삭제(비활성화)합니다."
    )
    @DeleteMapping("/books/{bookId}/goals")
    fun deleteGoal(
        @Parameter(description = "도서 ID") @PathVariable bookId: Long,
        @Parameter(hidden = true) @LoginUserId userId: Long
    ): ResponseEntity<ApiResponse<Nothing?>> {
        readingGoalService.deleteGoal(userId, bookId)
        return ResponseEntity.ok(ApiResponse.ok(null))
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
                achievedAmount = it.achievedAmount
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
        val result = readingLogService.createLog(
            userId = userId,
            bookId = bookId,
            bookStatus = request.bookStatus,
            recordDate = request.recordDate,
            readQuantity = request.readQuantity,
            durationSeconds = request.durationSeconds,
            rating = request.rating
        )

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(CreateReadingLogResponse(result.log.id, result.finishedCount)))
    }

    @Operation(
        summary = "독서 기록 상세 조회",
        description = """
            특정 책의 독서 기록 상세 정보를 조회합니다.
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