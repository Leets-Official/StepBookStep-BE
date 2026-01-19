package com.stepbookstep.server.domain.reading.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.Positive
import java.time.OffsetDateTime

@Entity
@Table(name = "reading_goals")
class ReadingGoal(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "book_id", nullable = false)
    val bookId: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var period: GoalPeriod,  // var로 변경 (수정 가능)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var metric: GoalMetric,

    @field:Positive(message = "목표량은 1 이상이어야 합니다")
    @Column(name = "target_amount", nullable = false)
    var targetAmount: Int,

    @Column(name = "is_active", nullable = false)
    var active: Boolean = true,

    @Column(name = "created_at", nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now()
)

enum class GoalPeriod {
    DAILY,
    WEEKLY,
    MONTHLY
}

enum class GoalMetric {
    TIME,
    PAGE
}