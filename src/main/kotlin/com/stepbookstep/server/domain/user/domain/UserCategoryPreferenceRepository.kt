package com.stepbookstep.server.domain.user.domain

import org.springframework.data.jpa.repository.JpaRepository

interface UserCategoryPreferenceRepository : JpaRepository<UserCategoryPreference, Long> {

    fun deleteAllByUserId(userId: Long)

    fun findAllByUserId(userId: Long): List<UserCategoryPreference>

}