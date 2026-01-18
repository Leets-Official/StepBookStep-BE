package com.stepbookstep.server.domain.onboarding.application

import com.stepbookstep.server.domain.onboarding.application.dto.NicknameCheckResponse
import com.stepbookstep.server.domain.onboarding.application.dto.OnboardingSaveRequest
import com.stepbookstep.server.domain.onboarding.application.dto.OnboardingSaveResponse
import com.stepbookstep.server.domain.user.domain.UserCategoryPreference
import com.stepbookstep.server.domain.user.domain.UserCategoryPreferenceRepository
import com.stepbookstep.server.domain.user.domain.UserRepository
import com.stepbookstep.server.global.response.CustomException
import com.stepbookstep.server.global.response.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 온보딩 저장
 * - 닉네임 중복 확인
 * - categoryIds 저장
 * - 레벨/루틴 계산
 * - User에 결과 반영
 */

@Service
class UserOnboardingService(
    private val userRepository: UserRepository,
    private val userCategoryPreferenceRepository: UserCategoryPreferenceRepository,
    private val ruleEngine: OnboardingRuleEngine
) {
    private val nicknameRegex = Regex("^[가-힣a-zA-Z0-9]{2,15}$")

    fun checkNickname(nickname: String): NicknameCheckResponse {
        val trimmed = nickname.trim()
        validateNickname(trimmed)

        return NicknameCheckResponse(
            nickname = trimmed,
            isAvailable = !userRepository.existsByNickname(trimmed)
        )
    }

    @Transactional
    fun saveOnboarding(userId: Long, request: OnboardingSaveRequest): OnboardingSaveResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { CustomException(ErrorCode.USER_NOT_FOUND) }

        validateNickname(request.nickname)

        if (userRepository.existsByNicknameAndIdNot(request.nickname, userId)) {
            throw CustomException(ErrorCode.DUPLICATED_NICKNAME)
        }

        user.nickname = request.nickname
        user.isOnboarded = true

        userCategoryPreferenceRepository.deleteAllByUserId(userId)

        val preferences = request.categoryIds.distinct().map { categoryId ->
            UserCategoryPreference(userId = userId, categoryId = categoryId)
        }
        userCategoryPreferenceRepository.saveAll(preferences)

        val result = ruleEngine.calculate(request.levelAnswers)

        user.applyOnboardingResult(
            nickname = request.nickname,
            level = result.level,
            routineType = result.routineType
        )

        return OnboardingSaveResponse(
            isOnboarded = true,
            level = result.level,
            routineType = result.routineType
        )
    }

    private fun validateNickname(nickname: String) {
        if (!nicknameRegex.matches(nickname)) {
            throw CustomException(ErrorCode.INVALID_INPUT)
        }
    }
}
