package com.stepbookstep.server.domain.reading.application

import com.stepbookstep.server.domain.book.domain.BookRepository
import com.stepbookstep.server.domain.mypage.domain.ReadStatus
import com.stepbookstep.server.domain.reading.domain.GoalMetric
import com.stepbookstep.server.domain.reading.domain.ReadingGoalRepository
import com.stepbookstep.server.domain.reading.domain.ReadingLog
import com.stepbookstep.server.domain.reading.domain.ReadingLogRepository
import com.stepbookstep.server.domain.reading.domain.ReadingLogStatus
import com.stepbookstep.server.domain.reading.domain.ReadingLogStatus.*
import com.stepbookstep.server.domain.reading.domain.UserBook
import com.stepbookstep.server.domain.reading.domain.UserBookRepository
import com.stepbookstep.server.global.response.CustomException
import com.stepbookstep.server.global.response.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.OffsetDateTime

@Service
class ReadingLogService(
    private val readingLogRepository: ReadingLogRepository,
    private val userBookRepository: UserBookRepository,
    private val bookRepository: BookRepository,
    private val readingGoalRepository: ReadingGoalRepository
) {

    @Transactional
    fun createLog(
        userId: Long,
        bookId: Long,
        bookStatus: ReadingLogStatus,
        recordDate: LocalDate?,
        readQuantity: Int?,
        durationSeconds: Int?,
        rating: Int?
    ): ReadingLog {
        validateBookExists(bookId)

        val activeGoal = validateByStatus(
            userId = userId,
            bookId = bookId,
            bookStatus = bookStatus,
            readQuantity = readQuantity,
            durationSeconds = durationSeconds,
            rating = rating
        )

        val book = bookRepository.findById(bookId)
            .orElseThrow { CustomException(ErrorCode.BOOK_NOT_FOUND) }

        val userBook = userBookRepository.findByUserIdAndBookId(userId, bookId)
            ?: userBookRepository.save(
                UserBook(
                    userId = userId,
                    book = book,
                    status = bookStatus.toReadStatus()
                )
            )

        val targetStatus = bookStatus.toReadStatus()

        if (userBook.status != targetStatus) {
            userBook.status = targetStatus
            userBook.updatedAt = OffsetDateTime.now()
        }

        // recordDate가 null이면 오늘 날짜 사용
        val actualRecordDate = recordDate ?: LocalDate.now()

        // 완독/중지 시에는 페이지/시간 무시, READING일 때만 저장
        val log = readingLogRepository.save(
            ReadingLog(
                userId = userId,
                bookId = bookId,
                bookStatus = bookStatus,
                recordDate = actualRecordDate,
                readQuantity = if (bookStatus == READING) readQuantity else null,
                durationSeconds = if (bookStatus == READING) durationSeconds else null,
                rating = if (bookStatus == READING) null else rating
            )
        )

        // 완독 또는 중지 시 활성 목표 비활성화
        if (bookStatus == FINISHED || bookStatus == STOPPED) {
            deactivateActiveGoalIfExists(userId, bookId)
        }

        return log
    }

    private fun validateBookExists(bookId: Long) {
        if (!bookRepository.existsById(bookId)) {
            throw CustomException(ErrorCode.BOOK_NOT_FOUND)
        }
    }

    /**
     * 상태별 검증.
     * READING이면 activeGoal을 반환해서 아래 로직(필요시)에 재사용 가능하게 할 수도 있음.
     */
    private fun validateByStatus(
        userId: Long,
        bookId: Long,
        bookStatus: ReadingLogStatus,
        readQuantity: Int?,
        durationSeconds: Int?,
        rating: Int?
    ) = when (bookStatus) {
        READING -> validateReadingLog(userId, bookId, readQuantity, durationSeconds)
        FINISHED -> {
            validateRating(rating)
            null
        }
        STOPPED -> {
            validateRating(rating)
            null
        }
    }

    private fun validateReadingLog(
        userId: Long,
        bookId: Long,
        readQuantity: Int?,
        durationSeconds: Int?
    ) = run {
        val activeGoal = readingGoalRepository.findByUserIdAndBookIdAndActiveTrue(userId, bookId)
            ?: throw CustomException(ErrorCode.GOAL_NOT_FOUND)

        // 읽은 페이지 필수 검증
        if (readQuantity == null) {
            throw CustomException(ErrorCode.READ_QUANTITY_REQUIRED)
        }

        // 페이지 역행 검증 (이전 기록보다 적은 페이지 입력 불가)
        validatePageProgression(userId, bookId, readQuantity)

        // 목표 metric에 따른 추가 검증
        validateByGoalMetric(activeGoal.metric, durationSeconds)

        activeGoal
    }

    /**
     * 페이지 역행 검증
     * - 이전 기록보다 적은 페이지를 입력할 수 없음
     */
    private fun validatePageProgression(userId: Long, bookId: Long, readQuantity: Int) {
        val latestRecord = readingLogRepository.findLatestRecordByUserIdAndBookId(userId, bookId).firstOrNull()

        if (latestRecord != null && latestRecord.readQuantity != null) {
            if (readQuantity < latestRecord.readQuantity!!) {
                throw CustomException(ErrorCode.PAGE_CANNOT_GO_BACK)
            }
        }
    }

    private fun validateByGoalMetric(metric: GoalMetric, durationSeconds: Int?) {
        when (metric) {
            GoalMetric.TIME -> {
                if (durationSeconds == null) {
                    throw CustomException(ErrorCode.DURATION_REQUIRED)
                }
            }
            GoalMetric.PAGE -> Unit
        }
    }

    private fun validateRating(rating: Int?) {
        when {
            rating == null -> throw CustomException(ErrorCode.RATING_REQUIRED)
            rating !in 1..5 -> throw CustomException(ErrorCode.INVALID_RATING)
        }
    }

    private fun deactivateActiveGoalIfExists(userId: Long, bookId: Long) {
        val activeGoal = readingGoalRepository.findByUserIdAndBookIdAndActiveTrue(userId, bookId)
        if (activeGoal != null) {
            activeGoal.active = false
            activeGoal.updatedAt = OffsetDateTime.now()
            readingGoalRepository.save(activeGoal)
        }
    }

    private fun ReadingLogStatus.toReadStatus(): ReadStatus = when (this) {
        READING -> ReadStatus.READING
        FINISHED -> ReadStatus.FINISHED
        STOPPED -> ReadStatus.STOPPED
    }
}