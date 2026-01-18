package com.stepbookstep.server.domain.onboarding.application.dto

import com.stepbookstep.server.domain.onboarding.domain.enum.RoutineType

data class OnboardingSaveResponse(
    val isOnboarded: Boolean,
    val level: Int,
    val routineType: RoutineType
)