package com.stepbookstep.server.domain.reading.application

import com.stepbookstep.server.domain.book.domain.BookRepository
import com.stepbookstep.server.domain.reading.domain.GoalMetric
import com.stepbookstep.server.domain.reading.domain.ReadingGoalRepository
import com.stepbookstep.server.domain.reading.domain.ReadingLog
import com.stepbookstep.server.domain.reading.domain.ReadingLogRepository
import com.stepbookstep.server.domain.reading.domain.ReadingLogStatus
import com.stepbookstep.server.domain.reading.domain.ReadingLogStatus.*
import com.stepbookstep.server.domain.reading.domain.UserBook
import com.stepbookstep.server.domain.reading.domain.UserBookRepository
import com.stepbookstep.server.domain.reading.domain.UserBookStatus
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
        recordDate: LocalDate,
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

        val userBook = userBookRepository.findByUserIdAndBookId(userId, bookId)
            ?: userBookRepository.save(
                UserBook(
                    userId = userId,
                    bookId = bookId,
                    status = bookStatus.toUserBookStatus()
                )
            )

        val targetStatus = bookStatus.toUserBookStatus()

        if (userBook.status != targetStatus) {
            userBook.status = targetStatus
            userBook.updatedAt = OffsetDateTime.now()
            userBookRepository.save(userBook)
        }

        val log = readingLogRepository.save(
            ReadingLog(
                userId = userId,
                bookId = bookId,
                bookStatus = bookStatus,
                recordDate = recordDate,
                readQuantity = readQuantity,
                durationSeconds = durationSeconds,
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

        requireReadQuantity(readQuantity)
        validateByGoalMetric(activeGoal.metric, durationSeconds)

        activeGoal
    }

    private fun requireReadQuantity(readQuantity: Int?) {
        if (readQuantity == null) {
            throw CustomException(ErrorCode.INVALID_INPUT)
        }
    }

    private fun validateByGoalMetric(metric: GoalMetric, durationSeconds: Int?) {
        when (metric) {
            GoalMetric.TIME -> {
                if (durationSeconds == null) {
                    throw CustomException(ErrorCode.INVALID_INPUT)
                }
            }
            GoalMetric.PAGE -> Unit
        }
    }

    private fun validateRating(rating: Int?) {
        if (rating == null || rating !in 1..5) {
            throw CustomException(ErrorCode.INVALID_INPUT)
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

    private fun ReadingLogStatus.toUserBookStatus(): UserBookStatus = when (this) {
        READING -> UserBookStatus.READING
        FINISHED -> UserBookStatus.FINISHED
        STOPPED -> UserBookStatus.STOPPED
    }
}