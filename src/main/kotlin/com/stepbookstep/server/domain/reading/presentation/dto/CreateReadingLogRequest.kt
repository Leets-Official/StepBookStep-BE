package com.stepbookstep.server.domain.reading.presentation.dto

import com.stepbookstep.server.domain.reading.domain.ReadingLogStatus
import java.time.LocalDate

data class CreateReadingLogRequest(
    val bookStatus: ReadingLogStatus,
    val recordDate: LocalDate,
    val readQuantity: Int? = null,
    val durationSeconds: Int? = null,
    val difficulty: String? = null
)