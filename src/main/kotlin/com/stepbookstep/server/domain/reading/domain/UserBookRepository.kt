package com.stepbookstep.server.domain.reading.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface UserBookRepository : JpaRepository<UserBook, Long> {

    @Query("SELECT ub FROM UserBook ub WHERE ub.userId = :userId AND ub.book.id = :bookId")
    fun findByUserIdAndBookId(@Param("userId") userId: Long, @Param("bookId") bookId: Long): UserBook?

    /**
     * 여러 책의 UserBook을 한 번에 조회 (N+1 해결)
     */
    @Query("""
        SELECT ub 
        FROM UserBook ub 
        JOIN FETCH ub.book 
        WHERE ub.userId = :userId 
        AND ub.book.id IN :bookIds
    """)
    fun findAllByUserIdAndBookIdIn(
        @Param("userId") userId: Long,
        @Param("bookIds") bookIds: List<Long>
    ): List<UserBook>

    /**
     * 사용자의 완독한 책 목록 조회 (통계용)
     */
    @Query("""
        SELECT ub
        FROM UserBook ub
        JOIN FETCH ub.book
        WHERE ub.userId = :userId
        AND ub.status = 'FINISHED'
        ORDER BY ub.finishedAt DESC
    """)
    fun findFinishedBooksByUserId(@Param("userId") userId: Long): List<UserBook>

    /**
     * 사용자의 읽는 중/완독한 책 목록 조회 (추천/검색용)
     */
    @Query("""
        SELECT ub
        FROM UserBook ub
        JOIN FETCH ub.book
        WHERE ub.userId = :userId
        AND ub.status IN ('READING', 'FINISHED')
    """)
    fun findReadingAndFinishedBooksByUserId(@Param("userId") userId: Long): List<UserBook>
}