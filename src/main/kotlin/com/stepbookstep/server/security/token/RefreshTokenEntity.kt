package com.stepbookstep.server.security.token

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "refresh_tokens")
class RefreshTokenEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "token_hash", nullable = false, unique = true, length = 255)
    val tokenHash: String,

    @Column(name = "expires_at", nullable = false)
    val expiresAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "revoked_at")
    var revokedAt: OffsetDateTime? = null,

    @Column(name = "replaced_by_token_hash")
    var replacedByTokenHash: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now()
) {
    //JPA용 기본 생성자
    protected constructor() : this(
        id = 0,
        userId = 0,
        tokenHash = "",
        expiresAt = OffsetDateTime.now(),
        revokedAt = null,
        replacedByTokenHash = null,
        createdAt = OffsetDateTime.now()
    )

    fun revokeNow() {
        this.revokedAt = OffsetDateTime.now()
    }

    fun isRevoked(): Boolean = revokedAt != null

    fun isExpired(now: OffsetDateTime = OffsetDateTime.now()): Boolean = expiresAt.isBefore(now)
}

