package com.stepbookstep.server.domain.mypage.application

import com.stepbookstep.server.domain.mypage.domain.ReadStatus
import com.stepbookstep.server.domain.reading.domain.UserBookRepository
import com.stepbookstep.server.global.response.CustomException
import com.stepbookstep.server.global.response.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

/**
 * user_books의 읽기 상태를 변경하는 클래스
 */
@Service
class ReadingStatusService(
    private val userBookRepository: UserBookRepository
) {

    @Transactional
    fun updateStatus(
        userId: Long,
        userBookId: Long,
        newStatus: ReadStatus,
        rating: Int?
    ) {
        if (userBookId <= 0) throw CustomException(ErrorCode.INVALID_INPUT)

        val userBook = userBookRepository.findById(userBookId)
            .orElseThrow { CustomException(ErrorCode.USER_BOOK_NOT_FOUND) }

        // 내 서재 항목(userBookId)은 해당 유저만 수정 가능
        if (userBook.userId != userId) {
            throw CustomException(ErrorCode.FORBIDDEN)
        }

        when (newStatus) {
            ReadStatus.NOT_STARTED -> {
                userBook.status = ReadStatus.NOT_STARTED
                userBook.finishedAt = null
                userBook.rating = null
            }

            ReadStatus.READING -> {
                userBook.status = ReadStatus.READING
                // 재독서시, 데이터 보존
            }

            ReadStatus.STOPPED -> {
                userBook.status = ReadStatus.STOPPED
            }

            ReadStatus.FINISHED -> {
                userBook.status = ReadStatus.FINISHED
                userBook.finishedAt = OffsetDateTime.now()

                if (rating != null) {
                    if (rating !in 1..5) throw CustomException(ErrorCode.INVALID_INPUT)
                    userBook.rating = rating
                }
            }
        }

        userBook.updatedAt = OffsetDateTime.now()
    }
}