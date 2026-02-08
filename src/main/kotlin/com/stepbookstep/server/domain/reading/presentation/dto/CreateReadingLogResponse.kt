package com.stepbookstep.server.domain.reading.presentation.dto

data class CreateReadingLogResponse(
    val recordId: Long,
    val finishedCount: Int?=null
)