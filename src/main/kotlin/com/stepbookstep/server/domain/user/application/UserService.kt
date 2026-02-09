package com.stepbookstep.server.domain.user.application

import com.stepbookstep.server.domain.user.domain.User
import com.stepbookstep.server.domain.user.domain.UserRepository
import com.stepbookstep.server.domain.user.domain.UserStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

/**
 * 사용자 관련 비즈니스 로직을 처리하는 서비스 클래스
 */
@Service
class UserService(
    private val userRepository: UserRepository
) {
    /**
     * 이미 가입된 회원이 있는지 조회
     */
    @Transactional(readOnly = true)
    fun findByKakaoProviderUserId(providerUserId: String): User? {
        return userRepository.findByProviderAndProviderUserId("KAKAO", providerUserId)
    }

    /**
     * 카카오 계정 정보를 바탕으로 새로운 사용자를 생성하고 저장
     */
    @Transactional
    fun createKakaoUser(providerUserId: String, nickname: String, email: String): User {
        val user = User(
            provider = "KAKAO",
            providerUserId = providerUserId,
            nickname = nickname,
            email = email
        )
        return userRepository.save(user)
    }

    /**
     * @return Pair(유저 객체, 신규 가입 여부)
     */
    @Transactional
    fun getOrCreateKakaoUser(providerUserId: String, nickname: String, email: String): Pair<User, Boolean> {
        val user = userRepository.findByProviderAndProviderUserId("KAKAO", providerUserId)

        if (user != null) {

            // 탈퇴 유저 복구
            if (user.status == UserStatus.WITHDRAWN) {
                user.status = UserStatus.ACTIVE

                user.nickname = nickname
                user.email = email
                user.updatedAt = OffsetDateTime.now()

                return user to false // 다시 가입 처리
            }

            return user to false
        }

        // 신규 가입
        val newUser = User(
            provider = "KAKAO",
            providerUserId = providerUserId,
            nickname = nickname,
            email = email,
            status = UserStatus.ACTIVE
        )

        return userRepository.save(newUser) to true
    }
}

