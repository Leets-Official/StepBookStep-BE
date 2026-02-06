package com.stepbookstep.server.domain.mypage.domain

import com.stepbookstep.server.global.response.CustomException
import com.stepbookstep.server.global.response.ErrorCode

/**
 * - user_books.status에 저장되는 "읽기 상태" Enum
 * - 북마크(읽고 싶은)는 status가 아니라 is_bookmarked(boolean)로 분리하여 관리
 */
enum class ReadStatus {
    READING, //독서상태 - '읽는 중'
    FINISHED, // 독서상태 - '완독한'
    STOPPED, // 독서상태 - '중단한'
    NOT_STARTED; // 읽기 전

    companion object {
        fun from(value: String): ReadStatus {
            val v = value.trim()
            if (v.isBlank()) throw CustomException(ErrorCode.INVALID_INPUT)

            return when (value.trim().uppercase()) {
                "READING" -> READING
                "FINISHED" -> FINISHED
                "STOPPED" -> STOPPED
                "NOT_STARTED" -> NOT_STARTED
                else -> throw CustomException(ErrorCode.INVALID_INPUT)
            }
        }
    }
}