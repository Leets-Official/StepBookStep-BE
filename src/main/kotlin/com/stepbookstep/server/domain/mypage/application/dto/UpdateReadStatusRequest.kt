package com.stepbookstep.server.domain.mypage.application.dto

import com.stepbookstep.server.domain.mypage.domain.ReadStatus

data class UpdateReadStatusRequest(
    val status: ReadStatus,
    val rating: Int? = null // 완독 시 별점(1~5). FINISHED일 때만 허용
)