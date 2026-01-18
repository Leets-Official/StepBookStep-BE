package com.stepbookstep.server.domain.user.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.transaction.annotation.Transactional

interface UserCategoryPreferenceRepository : JpaRepository<UserCategoryPreference, Long> {

    @Modifying
    @Transactional
    fun deleteAllByUserId(userId: Long)

    fun existsByUserIdAndCategoryId(userId: Long, categoryId: Int): Boolean
}