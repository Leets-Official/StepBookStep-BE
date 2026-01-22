package com.stepbookstep.server.domain.mypage.domain

import com.stepbookstep.server.global.response.CustomException
import com.stepbookstep.server.global.response.ErrorCode

/**
 * - API 요청/응답용
 * - readStatus 파라미터를 그대로 받되, BOOKMARKED 까지 포함하기 위해 별도 Enum으로 분리함
 */
enum class MyShelf {
    READING, //독서상태 - '읽는 중'
    FINISHED, // 독서상태 - '완독한'
    STOPPED, // 독서상태 - '중단한'
    BOOKMARKED; // 독서상태 - '읽고 싶은' (북마크)

    companion object {
        fun from(value: String): MyShelf {
            val v = value.trim()
            if (v.isBlank()) throw CustomException(ErrorCode.INVALID_INPUT)

            return when (v.uppercase()) {
                "READING" -> READING
                "FINISHED" -> FINISHED
                "STOPPED" -> STOPPED
                "BOOKMARKED" -> BOOKMARKED
                else -> throw CustomException(ErrorCode.INVALID_INPUT)
            }
        }
    }
}