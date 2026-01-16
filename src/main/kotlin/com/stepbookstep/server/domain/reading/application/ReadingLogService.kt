package com.stepbookstep.server.domain.reading.application

import com.stepbookstep.server.domain.book.domain.BookRepository
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
import java.time.OffsetDateTime

@Service
class ReadingLogService(
    private val readingLogRepository: ReadingLogRepository,
    private val userBookRepository: UserBookRepository,
    private val bookRepository: BookRepository
) {

    @Transactional
    fun createLog(
        userId: Long,
        bookId: Long,
        bookStatus: ReadingLogStatus,
        recordDate: java.time.LocalDate,
        readQuantity: Int?,
        durationSeconds: Int?,
        difficulty: String?
    ): ReadingLog {
        if (!bookRepository.existsById(bookId)) {
            throw CustomException(ErrorCode.BOOK_NOT_FOUND)
        }

        when (bookStatus) {
            READING -> {
                if (readQuantity == null || durationSeconds == null) {
                    throw CustomException(ErrorCode.INVALID_INPUT)
                }
            }
            FINISHED -> {
                if (difficulty.isNullOrBlank()) {
                    throw CustomException(ErrorCode.INVALID_INPUT)
                }
            }

            STOPPED -> TODO()
        }

        val userBook = userBookRepository.findByUserIdAndBookId(userId, bookId)
            ?: userBookRepository.save(
                UserBook(
                    userId = userId,
                    bookId = bookId,
                    status = when (bookStatus) {
                        READING -> UserBookStatus.READING
                        FINISHED -> UserBookStatus.FINISHED
                        STOPPED -> TODO()
                    }
                )
            )

        val targetStatus = when (bookStatus) {
            READING -> UserBookStatus.READING
            FINISHED -> UserBookStatus.FINISHED
            STOPPED -> TODO()
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
                difficulty = difficulty
            )
        )
    }
}