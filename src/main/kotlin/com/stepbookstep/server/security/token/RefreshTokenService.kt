package com.stepbookstep.server.security.token

import com.stepbookstep.server.global.response.CustomException
import com.stepbookstep.server.global.response.ErrorCode
import com.stepbookstep.server.security.jwt.JwtProperties
import com.stepbookstep.server.security.jwt.JwtProvider
import com.stepbookstep.server.security.jwt.TokenType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

/**
 * Refresh Token 저장/검증/회전을 담당
 * - 로그인 시: 새 refresh token hash 저장
 * - 재발급 시: 기존 토큰 revoke + 새 토큰 저장(회전)
 */
@Service
class RefreshTokenService(
    private val repo: RefreshTokenRepository,
    private val jwtProvider: JwtProvider,
    private val jwtProperties: JwtProperties,
    private val tokenHashUtil: TokenHashUtil
) {

    /**
     * 로그인 시 refresh token 저장 (원문 저장 X)
     */
    @Transactional
    fun save(userId: Long, refreshToken: String) {

        repo.deleteByUserId(userId)
        val tokenHash = tokenHashUtil.sha256(refreshToken)
        val expiresAt = OffsetDateTime.now().plusSeconds(jwtProperties.refresh.expiration / 1000)

        repo.save(
            RefreshTokenEntity(
                userId = userId,
                tokenHash = tokenHash,
                expiresAt = expiresAt
            )
        )
    }

    /**
     * refresh token 유효성 검증 후 userId 반환
     */
    @Transactional(readOnly = true)
    fun validateAndGetUserId(refreshToken: String): Long {
        // 1) JWT 자체 검증(서명/만료)
        if (jwtProvider.getTokenType(refreshToken) != TokenType.REFRESH) {
            throw CustomException(ErrorCode.INVALID_REFRESH_TOKEN)
        }

        val userId = try {
            jwtProvider.getUserId(refreshToken)
        } catch (e: CustomException) {
            throw CustomException(
                when (e.errorCode) {
                    ErrorCode.TOKEN_EXPIRED -> ErrorCode.EXPIRED_REFRESH_TOKEN
                    else -> ErrorCode.INVALID_REFRESH_TOKEN
                }
            )
        }

        // 2) DB 조회 (해시 기준)
        val tokenHash = tokenHashUtil.sha256(refreshToken)
        val entity = repo.findByTokenHash(tokenHash)
            ?: throw CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND)

        // 3) DB 만료/폐기 확인
        if (entity.isRevoked()) throw CustomException(ErrorCode.INVALID_REFRESH_TOKEN)
        if (entity.isExpired()) throw CustomException(ErrorCode.EXPIRED_REFRESH_TOKEN)

        // 4) 토큰 안의 userId와 DB userId 일치 확인
        if (entity.userId != userId) throw CustomException(ErrorCode.REFRESH_TOKEN_MISMATCH)

        return userId
    }

    /**
     * refresh token 회전(rotation) 적용 재발급
     * - 기존 refreshToken을 폐기(revoked)
     * - 새 refreshToken 발급 & 저장
     * - 새 accessToken 발급
     */
    @Transactional
    fun reissue(refreshToken: String): ReissueResult {
        val userId = validateAndGetUserId(refreshToken)

        // 기존 토큰 폐기 + replacedBy 기록
        val oldHash = tokenHashUtil.sha256(refreshToken)
        val oldEntity = repo.findByTokenHash(oldHash)
            ?: throw CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND)

        val newAccess = jwtProvider.createToken(userId, TokenType.ACCESS)
        val newRefresh = jwtProvider.createToken(userId, TokenType.REFRESH)

        val newHash = tokenHashUtil.sha256(newRefresh)
        oldEntity.revokedAt = OffsetDateTime.now()
        oldEntity.replacedByTokenHash = newHash

        // 새 refresh 저장
        save(userId, newRefresh)

        return ReissueResult(
            accessToken = newAccess,
            refreshToken = newRefresh
        )
    }

    /**
     * 로그아웃시 refreshToken을 서버에서 무효화(revoked)합니다.
     */
    @Transactional
    fun logout(refreshToken: String) {
        val tokenHash = tokenHashUtil.sha256(refreshToken)

        val entity = repo.findByTokenHash(tokenHash)
            ?: throw CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND)

        if (entity.isRevoked()) {
            throw CustomException(ErrorCode.INVALID_REFRESH_TOKEN)
        }

        if (entity.expiresAt.isBefore(OffsetDateTime.now())) {
            throw CustomException(ErrorCode.EXPIRED_REFRESH_TOKEN)
        }

        entity.revokeNow()
    }

    data class ReissueResult(
        val accessToken: String,
        val refreshToken: String
    )
}
