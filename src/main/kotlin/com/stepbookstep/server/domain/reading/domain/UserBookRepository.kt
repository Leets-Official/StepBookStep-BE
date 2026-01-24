package com.stepbookstep.server.domain.reading.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface UserBookRepository : JpaRepository<UserBook, Long> {
    @Query("SELECT ub FROM UserBook ub WHERE ub.userId = :userId AND ub.book.id = :bookId")
    fun findByUserIdAndBookId(@Param("userId") userId: Long, @Param("bookId") bookId: Long): UserBook?
}