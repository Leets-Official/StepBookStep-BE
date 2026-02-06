package com.stepbookstep.server.domain.mypage.application

import com.stepbookstep.server.domain.book.domain.BookRepository
import com.stepbookstep.server.domain.mypage.domain.ReadStatus
import com.stepbookstep.server.domain.mypage.domain.MyPageUserBookRepository
import com.stepbookstep.server.domain.reading.domain.UserBook
import com.stepbookstep.server.global.response.CustomException
import com.stepbookstep.server.global.response.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

/**
 * - 도서 북마크(읽고 싶은) 등록/해제 로직
 */
@Service
class BookmarkService(
    private val bookRepository: BookRepository,
    private val myPageUserBookRepository: MyPageUserBookRepository
) {

    @Transactional
    fun addBookmark(userId: Long, bookId: Long) {
        validateBookId(bookId)

        val book = bookRepository.findById(bookId)
            .orElseThrow { CustomException(ErrorCode.BOOK_NOT_FOUND) }
        val existing = myPageUserBookRepository.findByUserIdAndBookId(userId, bookId)

        if (existing == null) {
            myPageUserBookRepository.save(
                UserBook(
                    userId = userId,
                    book = book,
                    status = ReadStatus.NOT_STARTED,
                    isBookmarked = true,
                    createdAt = OffsetDateTime.now(),
                    updatedAt = OffsetDateTime.now()
                )
            )
        } else {
            existing.isBookmarked = true
            existing.updatedAt = OffsetDateTime.now()
        }
    }

    @Transactional
    fun removeBookmark(userId: Long, bookId: Long) {
        validateBookId(bookId)

        val existing = myPageUserBookRepository.findByUserIdAndBookId(userId, bookId)
            ?: throw CustomException(ErrorCode.BOOKMARK_NOT_FOUND)

        if (!existing.isBookmarked) {
            throw CustomException(ErrorCode.BAD_REQUEST)
        }

        existing.isBookmarked = false
        existing.updatedAt = OffsetDateTime.now()

        // 북마크만 있고 읽기 기록이 전혀 없다면 레코드 삭제하여 깔끔하게 정리
        val hasNoReadingData =
            existing.totalPagesRead == 0 &&
                    existing.totalDurationSec == 0 &&
                    existing.progressPercent == 0 &&
                    existing.finishedAt == null &&
                    existing.rating == null

        if (hasNoReadingData && existing.status == ReadStatus.STOPPED) {
            myPageUserBookRepository.delete(existing)
        }
    }

    private fun validateBookId(bookId: Long) {
        if (bookId <= 0) {
            throw CustomException(ErrorCode.INVALID_INPUT)
        }
    }
}