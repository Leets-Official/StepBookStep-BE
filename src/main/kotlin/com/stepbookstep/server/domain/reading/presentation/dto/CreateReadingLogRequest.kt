package com.stepbookstep.server.domain.reading.presentation.dto

import com.stepbookstep.server.domain.reading.domain.ReadingLogStatus
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Positive
import java.time.LocalDate

data class CreateReadingLogRequest(
    val bookStatus: ReadingLogStatus,
    val recordDate: LocalDate,

    @field:Positive(message = "읽은 페이지는 1 이상이어야 합니다")
    val readQuantity: Int? = null,

    @field:Positive(message = "독서 시간은 1 이상이어야 합니다")
    val durationSeconds: Int? = null,

    @field:Min(value = 1, message = "별점은 1 이상이어야 합니다")
    @field:Max(value = 5, message = "별점은 5 이하여야 합니다")
    val rating: Int? = null  // 1~5 별점
)