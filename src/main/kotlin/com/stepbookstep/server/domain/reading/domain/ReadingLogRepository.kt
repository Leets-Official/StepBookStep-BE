package com.stepbookstep.server.domain.reading.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface ReadingLogRepository : JpaRepository<ReadingLog, Long> {

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
    """)
    fun findLastRecordInDateRange(
        @Param("userId") userId: Long,
        @Param("bookId") bookId: Long,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<ReadingLog>

    /**
     * 특정 기간 동안 사용자가 읽은 총 시간(초) 합계
     */
    @Query("""
        SELECT COALESCE(SUM(rl.durationSeconds), 0)
        FROM ReadingLog rl
        WHERE rl.userId = :userId
        AND rl.bookId = :bookId
        AND rl.recordDate BETWEEN :startDate AND :endDate
    """)
    fun sumDurationByUserIdAndBookIdAndDateRange(
        @Param("userId") userId: Long,
        @Param("bookId") bookId: Long,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): Int

    /**
     * 특정 날짜 이전의 마지막 기록 조회
     */
    @Query("""
        SELECT rl
        FROM ReadingLog rl
        WHERE rl.userId = :userId
        AND rl.bookId = :bookId
        AND rl.recordDate < :beforeDate
        AND rl.readQuantity IS NOT NULL
        ORDER BY rl.recordDate DESC, rl.createdAt DESC
    """)
    fun findLastRecordBeforeDate(
        @Param("userId") userId: Long,
        @Param("bookId") bookId: Long,
        @Param("beforeDate") beforeDate: LocalDate
    ): List<ReadingLog>

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
    """)
    fun findLatestRecordByUserIdAndBookId(
        @Param("userId") userId: Long,
        @Param("bookId") bookId: Long
    ): List<ReadingLog>

    /**
     * 사용자의 전체 독서 시간 합계 (초)
     */
    @Query("""
        SELECT COALESCE(SUM(rl.durationSeconds), 0)
        FROM ReadingLog rl
        WHERE rl.userId = :userId
    """)
    fun sumAllDurationByUserId(@Param("userId") userId: Long): Long

    /**
     * 특정 월에 완독한 책 수 계산
     */
    @Query("""
        SELECT COUNT(DISTINCT rl.bookId)
        FROM ReadingLog rl
        WHERE rl.userId = :userId
        AND rl.bookStatus = 'FINISHED'
        AND rl.recordDate BETWEEN :startDate AND :endDate
    """)
    fun countFinishedBooksInMonth(
        @Param("userId") userId: Long,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): Int
    /**
     * 여러 책의 모든 독서 기록 조회 (목표 달성률 계산 최적화용)
     */
    @Query("""
        SELECT rl
        FROM ReadingLog rl
        WHERE rl.userId = :userId
        AND rl.bookId IN :bookIds
        AND rl.recordDate >= :startDate
        ORDER BY rl.bookId, rl.recordDate ASC, rl.createdAt ASC
    """)
    fun findAllByBooksInDateRange(
        @Param("userId") userId: Long,
        @Param("bookIds") bookIds: List<Long>,
        @Param("startDate") startDate: LocalDate
    ): List<ReadingLog>

    /**
     * 연도별 월별 완독 책 수 집계 (월별 그래프 최적화용)
     * 단일 쿼리로 모든 월 데이터 조회
     */
    @Query("""
        SELECT FUNCTION('MONTH', rl.recordDate) as month, COUNT(DISTINCT rl.bookId) as count
        FROM ReadingLog rl
        WHERE rl.userId = :userId
        AND rl.bookStatus = 'FINISHED'
        AND FUNCTION('YEAR', rl.recordDate) = :year
        GROUP BY FUNCTION('MONTH', rl.recordDate)
    """)
    fun countFinishedBooksGroupedByMonth(
        @Param("userId") userId: Long,
        @Param("year") year: Int
    ): List<Array<Any>>
}