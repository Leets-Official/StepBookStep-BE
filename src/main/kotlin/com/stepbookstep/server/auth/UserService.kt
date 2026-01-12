package com.stepbookstep.server.auth

import com.stepbookstep.server.user.User
import com.stepbookstep.server.user.UserRepository
import org.springframework.stereotype.Service

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
    fun findByKakaoProviderUserId(providerUserId: String): User? {
        return userRepository.findByProviderAndProviderUserId("KAKAO", providerUserId)
    }

    /**
     * 카카오 계정 정보를 바탕으로 새로운 사용자를 생성하고 저장
     */
    fun createKakaoUser(providerUserId: String, nickname: String): User {
        val user = User(
            provider = "KAKAO",
            providerUserId = providerUserId,
            nickname = nickname,
        )
        return userRepository.save(user)
    }
}

