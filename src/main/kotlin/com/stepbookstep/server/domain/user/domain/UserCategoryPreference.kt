package com.stepbookstep.server.domain.user.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "user_category_preferences",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_user_category",
            columnNames = ["user_id", "category_id", "genre_id"]
        )
    ]
)
class UserCategoryPreference(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "category_id", nullable = false)
    val categoryId: Long,

    @Column(name = "genre_id", nullable = false)
    val genreId: Long,

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
)