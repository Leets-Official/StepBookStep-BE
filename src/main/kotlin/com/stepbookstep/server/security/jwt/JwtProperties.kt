package com.stepbookstep.server.security.jwt

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * JWT 설정 정보를 application.yml 에서 바인딩하기 위한 클래스
 *
 */

@ConfigurationProperties(prefix = "stepbookstep.jwt")
data class JwtProperties(
    val key: String,
    val access: Expiration,
    val refresh: Expiration
) {
    data class Expiration(
        val expiration: Long // ms
    )
}
