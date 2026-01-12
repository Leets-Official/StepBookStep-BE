package com.stepbookstep.server.security.jwt

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

/**
 * JWT 설정 정보를 application-local.yml 에서 바인딩
 */

@ConfigurationProperties(prefix = "stepbookstep.jwt")
data class JwtProperties @ConstructorBinding constructor(
    val key: String,
    val access: Expiration,
    val refresh: Expiration
) {
    data class Expiration(
        val expiration: Long // ms
    )
}
