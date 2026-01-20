package com.stepbookstep.server.domain.reading.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface ReadingLogRepository : JpaRepository<ReadingLog, Long> {

    /**
     * 특정 사용자의 특정 책에 대한 전체 누적 읽은 페이지 수
     */
    @Query("""
        SELECT COALESCE(SUM(rl.readQuantity), 0)
        FROM ReadingLog rl
        WHERE rl.userId = :userId
        AND rl.bookId = :bookId
        AND rl.readQuantity IS NOT NULL
    """)
    fun sumTotalReadQuantityByUserIdAndBookId(
        @Param("userId") userId: Long,
        @Param("bookId") bookId: Long
    ): Int

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
    ): Int

    /**
     * 특정 기간의 첫 번째 기록 조회 (가장 이른 날짜)
     */
    @Query("""
        SELECT rl
        FROM ReadingLog rl
        WHERE rl.userId = :userId
        AND rl.bookId = :bookId
        AND rl.recordDate BETWEEN :startDate AND :endDate
        AND rl.readQuantity IS NOT NULL
        ORDER BY rl.recordDate ASC, rl.createdAt ASC
        LIMIT 1
    """)
    fun findFirstRecordInDateRange(
        @Param("userId") userId: Long,
        @Param("bookId") bookId: Long,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): ReadingLog?

    /**
     * 특정 기간의 마지막 기록 조회 (가장 늦은 날짜)
     */
    @Query("""
        SELECT rl
        FROM ReadingLog rl
        WHERE rl.userId = :userId
        AND rl.bookId = :bookId
        AND rl.recordDate BETWEEN :startDate AND :endDate
        AND rl.readQuantity IS NOT NULL
        ORDER BY rl.recordDate DESC, rl.createdAt DESC
        LIMIT 1
    """)
    fun findLastRecordInDateRange(
        @Param("userId") userId: Long,
        @Param("bookId") bookId: Long,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): ReadingLog?

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
    ): Int

    /**
     * 사용자의 특정 책에 대한 가장 최근 기록 조회
     */
    @Query("""
        SELECT rl
        FROM ReadingLog rl
        WHERE rl.userId = :userId
        AND rl.bookId = :bookId
        AND rl.readQuantity IS NOT NULL
        ORDER BY rl.recordDate DESC, rl.createdAt DESC
        LIMIT 1
    """)
    fun findLatestRecordByUserIdAndBookId(
        @Param("userId") userId: Long,
        @Param("bookId") bookId: Long
    ): ReadingLog?
}