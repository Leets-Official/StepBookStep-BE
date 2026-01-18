package com.stepbookstep.server.domain.user.domain

import com.stepbookstep.server.domain.onboarding.domain.enum.RoutineType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime

@Entity
@Table(name = "users")
class User(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val provider: String,

    @Column(name = "provider_user_id", nullable = false, unique = true)
    val providerUserId: String,

    @Column(nullable = false)
    var nickname: String,

    @Column(nullable = false)
    var status: String = "ACTIVE",

    @Column(name = "created_at", nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now(),

    // ===== 온보딩 결과 저장 =====
    @Column(name = "level", nullable = false)
    var level: Int = 1,

    @Enumerated(EnumType.STRING)
    @Column(name = "routine_type", length = 20)
    var routineType: RoutineType? = null,

    @Column(name = "is_onboarded", nullable = false)
    var isOnboarded: Boolean = false,
) {
    fun applyOnboardingResult(
        nickname: String,
        level: Int,
        routineType: RoutineType
    ) {
        this.nickname = nickname
        this.level = level
        this.routineType = routineType
        this.isOnboarded = true
        this.updatedAt = OffsetDateTime.now()
    }
}