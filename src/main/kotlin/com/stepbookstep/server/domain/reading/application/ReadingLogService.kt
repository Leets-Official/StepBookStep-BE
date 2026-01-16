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
        if (!bookRepository.existsById(bookId)) {
            throw CustomException(ErrorCode.BOOK_NOT_FOUND)
        }

        when (bookStatus) {
            READING -> {
                // 활성 목표 조회
                val activeGoal = readingGoalRepository.findByUserIdAndBookIdAndActiveTrue(userId, bookId)
                    ?: throw CustomException(ErrorCode.GOAL_NOT_FOUND)

                // 목표 metric에 따른 검증
                when (activeGoal.metric) {
                    GoalMetric.TIME -> {
                        // 시간 목표: 쪽수 + 시간 둘 다 입력 가능, 최소 하나는 필수
                        if (readQuantity == null && durationSeconds == null) {
                            throw CustomException(ErrorCode.INVALID_INPUT)
                        }
                    }
                    GoalMetric.PAGE -> {
                        // 쪽수 목표: 쪽수만 필수
                        if (readQuantity == null) {
                            throw CustomException(ErrorCode.INVALID_INPUT)
                        }
                    }
                }
            }
            FINISHED -> {
                if (rating == null || rating !in 1..5) {
                    throw CustomException(ErrorCode.INVALID_INPUT)
                }
            }
            STOPPED -> {
                // STOPPED 로직 추가 필요
                TODO()
            }
        }

        val userBook = userBookRepository.findByUserIdAndBookId(userId, bookId)
            ?: userBookRepository.save(
                UserBook(
                    userId = userId,
                    bookId = bookId,
                    status = when (bookStatus) {
                        READING -> UserBookStatus.READING
                        FINISHED -> UserBookStatus.FINISHED
                        STOPPED -> UserBookStatus.STOPPED
                    }
                )
            )

        val targetStatus = when (bookStatus) {
            READING -> UserBookStatus.READING
            FINISHED -> UserBookStatus.FINISHED
            STOPPED -> UserBookStatus.STOPPED
        }

        if (userBook.status != targetStatus) {
            userBook.status = targetStatus
            userBook.updatedAt = OffsetDateTime.now()
            userBookRepository.save(userBook)
        }

        return readingLogRepository.save(
            ReadingLog(
                userId = userId,
                bookId = bookId,
                bookStatus = bookStatus,
                recordDate = recordDate,
                readQuantity = readQuantity,
                durationSeconds = durationSeconds,
                rating = rating
            )
        )
    }
}