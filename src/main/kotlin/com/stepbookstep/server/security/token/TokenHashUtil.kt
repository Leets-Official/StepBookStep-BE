package com.stepbookstep.server.security.token

/**
 * Refresh Token 원문을 DB에 저장하지 않고, 해시로만 저장하기 위한 유틸 클래스
 * - 비교/검증 목적
 */

import org.springframework.stereotype.Component
import java.security.MessageDigest

@Component
class TokenHashUtil {

    fun sha256(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(token.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
