package com.stepbookstep.server.domain.reading.domain

import org.springframework.data.jpa.repository.JpaRepository

interface ReadingGoalRepository : JpaRepository<ReadingGoal, Long> {
    fun findByUserIdAndBookIdAndActiveTrue(userId: Long, bookId: Long): ReadingGoal?

    /**
     * 활성 목표를 최근 생성순으로 조회
     */
    fun findAllByUserIdAndActiveTrueOrderByCreatedAtDesc(userId: Long): List<ReadingGoal>

    /**
     * 가장 최근에 생성된 목표 조회 (활성/비활성 무관)
     * 완독/중지 후에도 목표를 표시하기 위해 사용
     */
    fun findTopByUserIdAndBookIdOrderByCreatedAtDesc(userId: Long, bookId: Long): ReadingGoal?
}