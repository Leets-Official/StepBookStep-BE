package com.stepbookstep.server.domain.reading.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface UserBookRepository : JpaRepository<UserBook, Long> {
    fun findByUserIdAndBookId(userId: Long, bookId: Long): UserBook?

    /**
     * 사용자의 완독한 책 목록 조회
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
}