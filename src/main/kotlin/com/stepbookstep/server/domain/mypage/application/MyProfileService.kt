package com.stepbookstep.server.domain.mypage.application

import com.stepbookstep.server.domain.mypage.application.dto.UpdateNicknameRequest
import com.stepbookstep.server.domain.mypage.application.dto.UpdatePreferencesRequest
import com.stepbookstep.server.domain.user.domain.UserCategoryPreference
import com.stepbookstep.server.domain.user.domain.UserCategoryPreferenceRepository
import com.stepbookstep.server.domain.user.domain.UserRepository
import com.stepbookstep.server.external.kakao.KakaoUnlinkClient
import com.stepbookstep.server.global.response.CustomException
import com.stepbookstep.server.global.response.ErrorCode
import com.stepbookstep.server.security.token.RefreshTokenRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import org.slf4j.LoggerFactory

@Service
class MyProfileService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val userCategoryPreferenceRepository: UserCategoryPreferenceRepository,
    private val kakaoUnlinkClient: KakaoUnlinkClient

) {
    companion object {
        private val VALID_LEVELS = setOf(1, 2, 3)
        private val log = LoggerFactory.getLogger(MyProfileService::class.java)
    }
    /**
     * 사용자의 선호 레벨/분야를 수정
     */
    @Transactional
    fun updatePreferences(userId: Long, request: UpdatePreferencesRequest) {
        val user = userRepository.findById(userId)
            .orElseThrow { CustomException(ErrorCode.USER_NOT_FOUND) }

        if (request.level !in VALID_LEVELS) {
            throw CustomException(ErrorCode.INVALID_INPUT)
        }

        val pairs = request.preferences
            .map { it.categoryId to it.genreId }
            .distinct()

        if (pairs.isEmpty()) {
            throw CustomException(ErrorCode.INVALID_INPUT)
        }

        user.level = request.level
        user.updatedAt = OffsetDateTime.now()

        userCategoryPreferenceRepository.deleteAllByUserId(userId)

        val entities = pairs.map { (categoryId, genreId) ->
            UserCategoryPreference(
                userId = userId,
                categoryId = categoryId,
                genreId = genreId
            )
        }
        userCategoryPreferenceRepository.saveAll(entities)
    }

    /**
     * 닉네임 수정
     * - 공백/길이/중복 검증
     */
    private val NICKNAME_REGEX = Regex("^[a-zA-Z0-9가-힣]{2,15}$")

    @Transactional
    fun updateNickname(userId: Long, request: UpdateNicknameRequest) {
        val user = userRepository.findById(userId)
            .orElseThrow { CustomException(ErrorCode.USER_NOT_FOUND) }

        val newNickname = request.nickname.trim()
        if (!NICKNAME_REGEX.matches(newNickname)) {
            throw CustomException(ErrorCode.INVALID_INPUT)
        }

        if (userRepository.existsByNickname(newNickname) && user.nickname != newNickname) {
            throw CustomException(ErrorCode.DUPLICATED_NICKNAME)
        }

        user.nickname = newNickname
        user.updatedAt = OffsetDateTime.now()
    }

    /**
     * 회원 탈퇴
     * - 카카오 unlink
     * - 인증 데이터 삭제
     * - 계정 탈퇴 처리
     */
    @Transactional
    fun deleteAccount(userId: Long) {
        val user = userRepository.findById(userId)
            .orElseThrow { CustomException(ErrorCode.USER_NOT_FOUND) }

        user.providerUserId.let { kakaoUserId ->
            try {
                kakaoUnlinkClient.unlink(kakaoUserId)
            } catch (e: Exception) {
                log.warn(
                    "Kakao unlink failed. userId={}, kakaoUserId={}",
                    userId,
                    kakaoUserId,
                    e
                )
            }
        }

        refreshTokenRepository.deleteByUserId(userId)

        user.status = "WITHDRAWN"
        user.updatedAt = OffsetDateTime.now()
    }
}