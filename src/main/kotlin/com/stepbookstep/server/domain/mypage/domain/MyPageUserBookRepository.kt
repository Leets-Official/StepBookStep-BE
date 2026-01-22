package com.stepbookstep.server.domain.mypage.domain

import com.stepbookstep.server.domain.reading.domain.UserBook
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * - 내 서재 조회 / 북마크 조회 / 북마크 여부 체크 등에 필요한 JPA Repository
 */
@Repository
interface MyPageUserBookRepository : JpaRepository<UserBook, Long> {

    fun findByUserIdAndBook_Id(userId: Long, bookId: Long): UserBook?

    fun existsByUserIdAndBook_IdAndIsBookmarkedTrue(userId: Long, bookId: Long): Boolean

    @EntityGraph(attributePaths = ["book"])
    fun findByUserIdAndStatus(userId: Long, status: ReadStatus, pageable: Pageable): Page<UserBook>

    @EntityGraph(attributePaths = ["book"])
    fun findByUserIdAndIsBookmarkedTrue(userId: Long, pageable: Pageable): Page<UserBook>
}