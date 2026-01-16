package com.stepbookstep.server.domain.reading.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface ReadingLogRepository : JpaRepository<ReadingLog, Long> {

    /**
     * 특정 기간 동안 사용자가 읽은 총 페이지 수 합계
     */
    @Query("""
        SELECT COALESCE(SUM(rl.readQuantity), 0)
        FROM ReadingLog rl
        WHERE rl.userId = :userId
        AND rl.bookId = :bookId
        AND rl.recordDate BETWEEN :startDate AND :endDate
        AND rl.readQuantity IS NOT NULL
    """)
    fun sumReadQuantityByUserIdAndBookIdAndDateRange(
        @Param("userId") userId: Long,
        @Param("bookId") bookId: Long,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): Int?

    /**
     * 특정 기간 동안 사용자가 읽은 총 시간(초) 합계
     */
    @Query("""
        SELECT COALESCE(SUM(rl.durationSeconds), 0)
        FROM ReadingLog rl
        WHERE rl.userId = :userId
        AND rl.bookId = :bookId
        AND rl.recordDate BETWEEN :startDate AND :endDate
        AND rl.durationSeconds IS NOT NULL
    """)
    fun sumDurationByUserIdAndBookIdAndDateRange(
        @Param("userId") userId: Long,
        @Param("bookId") bookId: Long,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): Int?
}