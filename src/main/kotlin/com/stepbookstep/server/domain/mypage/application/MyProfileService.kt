package com.stepbookstep.server.domain.mypage.application

import com.stepbookstep.server.domain.mypage.application.dto.UpdateNicknameRequest
import com.stepbookstep.server.domain.mypage.application.dto.UpdatePreferencesRequest
import com.stepbookstep.server.domain.user.domain.UserCategoryPreference
import com.stepbookstep.server.domain.user.domain.UserCategoryPreferenceRepository
import com.stepbookstep.server.domain.user.domain.UserGenrePreference
import com.stepbookstep.server.domain.user.domain.UserGenrePreferenceRepository
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
    private val userGenrePreferenceRepository: UserGenrePreferenceRepository,
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

        val requestCategoryIds = request.categoryIds.distinct().toSet()
        val requestGenreIds = request.genreIds.distinct().toSet()

        // 존재 여부 검증
        if (!userCategoryPreferenceRepository.existsAllByIds(requestCategoryIds)) {
            throw CustomException(ErrorCode.INVALID_INPUT)
        }

        if (!userGenrePreferenceRepository.existsAllByIds(requestGenreIds)) {
            throw CustomException(ErrorCode.INVALID_INPUT)
        }

        user.level = request.level
        user.updatedAt = OffsetDateTime.now()

        // ===== category 처리 =====
        val existingCategories = userCategoryPreferenceRepository.findAllByUserId(userId)
        val existingCategoryIds = existingCategories.map { it.categoryId }.toSet()

        val deleteCategoryIds = existingCategoryIds - requestCategoryIds
        if (deleteCategoryIds.isNotEmpty()) {
            userCategoryPreferenceRepository.deleteByUserIdAndCategoryIdIn(userId, deleteCategoryIds)
        }

        val insertCategoryIds = requestCategoryIds - existingCategoryIds
        if (insertCategoryIds.isNotEmpty()) {
            val inserts = insertCategoryIds.map { cid ->
                UserCategoryPreference(userId = userId, categoryId = cid)
            }
            userCategoryPreferenceRepository.saveAll(inserts)
        }

        // ===== genre 처리 =====
        val existingGenres = userGenrePreferenceRepository.findAllByUserId(userId)
        val existingGenreIds = existingGenres.map { it.genreId }.toSet()

        val deleteGenreIds = existingGenreIds - requestGenreIds
        if (deleteGenreIds.isNotEmpty()) {
            userGenrePreferenceRepository.deleteByUserIdAndGenreIdIn(userId, deleteGenreIds)
        }

        val insertGenreIds = requestGenreIds - existingGenreIds
        if (insertGenreIds.isNotEmpty()) {
            val inserts = insertGenreIds.map { gid ->
                UserGenrePreference(userId = userId, genreId = gid)
            }
            userGenrePreferenceRepository.saveAll(inserts)
        }
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

        if (user.provider == "KAKAO") {
            kakaoUnlinkClient.unlink(user.providerUserId)
        }

        refreshTokenRepository.deleteByUserId(userId)

        user.status = "WITHDRAWN"
        user.updatedAt = OffsetDateTime.now()
    }
}