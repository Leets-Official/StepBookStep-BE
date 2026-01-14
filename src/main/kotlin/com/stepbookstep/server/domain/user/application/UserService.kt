package com.stepbookstep.server.domain.user.application

import com.stepbookstep.server.domain.user.domain.User
import com.stepbookstep.server.domain.user.domain.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

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
    fun createKakaoUser(providerUserId: String, nickname: String): User {
        val user = User(
            provider = "KAKAO",
            providerUserId = providerUserId,
            nickname = nickname,
        )
        return userRepository.save(user)
    }

    /**
     * @return Pair(유저 객체, 신규 가입 여부)
     */
    @Transactional
    fun getOrCreateKakaoUser(providerUserId: String, nickname: String): Pair<User, Boolean> {
        val existingUser = userRepository.findByProviderAndProviderUserId("KAKAO", providerUserId)

        return if (existingUser != null) {
            existingUser to false
        } else {
            val newUser = User(
                provider = "KAKAO",
                providerUserId = providerUserId,
                nickname = nickname,
            )
            userRepository.save(newUser) to true
        }
    }
}

