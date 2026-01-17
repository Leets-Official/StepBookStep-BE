package com.stepbookstep.server.domain.reading.domain

import org.springframework.data.jpa.repository.JpaRepository

interface UserBookRepository : JpaRepository<UserBook, Long> {
    fun findByUserIdAndBookId(userId: Long, bookId: Long): UserBook?
}