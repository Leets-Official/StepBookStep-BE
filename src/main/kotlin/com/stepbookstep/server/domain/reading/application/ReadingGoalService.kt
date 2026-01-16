package com.stepbookstep.server.domain.reading.application

import com.stepbookstep.server.domain.book.domain.BookRepository
import com.stepbookstep.server.domain.reading.domain.GoalMetric
import com.stepbookstep.server.domain.reading.domain.GoalPeriod
import com.stepbookstep.server.domain.reading.domain.ReadingGoal
import com.stepbookstep.server.domain.reading.domain.ReadingGoalRepository
import com.stepbookstep.server.domain.reading.domain.UserBook
import com.stepbookstep.server.domain.reading.domain.UserBookRepository
import com.stepbookstep.server.domain.reading.domain.UserBookStatus
import com.stepbookstep.server.global.response.CustomException
import com.stepbookstep.server.global.response.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReadingGoalService(
    private val readingGoalRepository: ReadingGoalRepository,
    private val userBookRepository: UserBookRepository,
    private val bookRepository: BookRepository
) {

    @Transactional
    fun createGoal(
        userId: Long,
        bookId: Long,
        period: GoalPeriod,
        metric: GoalMetric,
        targetAmount: Int
    ): ReadingGoal {
        if (!bookRepository.existsById(bookId)) {
            throw CustomException(ErrorCode.BOOK_NOT_FOUND)
        }

        val userBook = userBookRepository.findByUserIdAndBookId(userId, bookId)
            ?: userBookRepository.save(
                UserBook(
                    userId = userId,
                    bookId = bookId,
                    status = UserBookStatus.WANT_TO_READ
                )
            )

        if (userBook.status != UserBookStatus.READING) {
            userBook.status = UserBookStatus.READING
            userBook.updatedAt = java.time.OffsetDateTime.now()
            userBookRepository.save(userBook)
        }

        val existingGoal = readingGoalRepository.findByUserIdAndBookIdAndActiveTrue(userId, bookId)
        if (existingGoal != null) {
            existingGoal.active = false
            readingGoalRepository.save(existingGoal)
        }

        return readingGoalRepository.save(
            ReadingGoal(
                userId = userId,
                bookId = bookId,
                period = period,
                metric = metric,
                targetAmount = targetAmount,
                active = true
            )
        )
    }

    @Transactional(readOnly = true)
    fun getActiveGoal(userId: Long, bookId: Long): ReadingGoal? {
        return readingGoalRepository.findByUserIdAndBookIdAndActiveTrue(userId, bookId)
    }

    @Transactional(readOnly = true)
    fun getActiveGoals(userId: Long): List<ReadingGoal> {
        return readingGoalRepository.findAllByUserIdAndActiveTrue(userId)
    }
}