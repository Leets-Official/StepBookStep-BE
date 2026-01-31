package com.stepbookstep.server.domain.onboarding.application

import com.stepbookstep.server.domain.onboarding.application.dto.NicknameCheckResponse
import com.stepbookstep.server.domain.onboarding.application.dto.OnboardingSaveRequest
import com.stepbookstep.server.domain.onboarding.application.dto.OnboardingSaveResponse
import com.stepbookstep.server.domain.onboarding.application.dto.RoutineTokens
import com.stepbookstep.server.domain.user.domain.UserCategoryPreference
import com.stepbookstep.server.domain.user.domain.UserCategoryPreferenceRepository
import com.stepbookstep.server.domain.user.domain.UserGenrePreference
import com.stepbookstep.server.domain.user.domain.UserGenrePreferenceRepository
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
    private val userGenrePreferenceRepository: UserGenrePreferenceRepository,
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

        val nickname = request.nickname.trim()
        validateNickname(request.nickname)

        if (userRepository.existsByNicknameAndIdNot(request.nickname, userId)) {
            throw CustomException(ErrorCode.DUPLICATED_NICKNAME)
        }

        validatePreferencesLimit(request)

        userCategoryPreferenceRepository.deleteAllByUserId(userId)
        userGenrePreferenceRepository.deleteAllByUserId(userId)

        val categoryIds = request.categoryIds.distinct()
        val genreIds = request.genreIds.distinct()

        if (categoryIds.isNotEmpty()) {
            userCategoryPreferenceRepository.saveAll(
                categoryIds.map { cid ->
                    UserCategoryPreference(userId = userId, categoryId = cid)
                }
            )
        }

        if (genreIds.isNotEmpty()) {
            userGenrePreferenceRepository.saveAll(
                genreIds.map { gid ->
                    UserGenrePreference(userId = userId, genreId = gid)
                }
            )
        }

        val preferenceCount = categoryIds.size + genreIds.size

        val tokens = ruleEngine.createTokens(
            answers = request.levelAnswers,
            preferenceCount = preferenceCount
        )

        user.completeOnboarding(nickname)
        user.level = if (preferenceCount == 0) 1 else 2

        return OnboardingSaveResponse(
            isOnboarded = true,
            level = user.level,
            routineTokens = RoutineTokens(
                period = tokens.periodToken,
                amount = tokens.amountToken,
                basis = tokens.basisToken
            )
        )
    }

    private fun validateNickname(nickname: String) {
        if (!nicknameRegex.matches(nickname)) {
            throw CustomException(ErrorCode.INVALID_INPUT)
        }
    }

    private fun validatePreferencesLimit(request: OnboardingSaveRequest) {
        val total = request.categoryIds.distinct().size + request.genreIds.distinct().size
        if (total > 3) {
            throw CustomException(ErrorCode.INVALID_INPUT)
        }
    }
}
