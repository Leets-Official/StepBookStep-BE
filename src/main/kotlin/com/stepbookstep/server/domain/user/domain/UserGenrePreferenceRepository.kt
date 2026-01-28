package com.stepbookstep.server.domain.user.domain

import org.springframework.data.jpa.repository.JpaRepository

interface UserGenrePreferenceRepository : JpaRepository<UserGenrePreference, Long> {
    fun findAllByUserId(userId: Long): List<UserGenrePreference>
    fun deleteAllByUserId(userId: Long)
    fun deleteByUserIdAndGenreIdIn(userId: Long, genreIds: Set<Long>)
    fun existsAllByIds(ids: Set<Long>): Boolean {
        return ids.all { existsById(it) }
    }
}