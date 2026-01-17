package com.stepbookstep.server.domain.reading.domain

import org.springframework.data.jpa.repository.JpaRepository

interface ReadingGoalRepository : JpaRepository<ReadingGoal, Long> {
    fun findByUserIdAndBookIdAndActiveTrue(userId: Long, bookId: Long): ReadingGoal?
    fun findAllByUserIdAndActiveTrue(userId: Long): List<ReadingGoal>
}