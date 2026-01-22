package com.stepbookstep.server.domain.reading.domain

import com.stepbookstep.server.domain.book.domain.Book
import com.stepbookstep.server.domain.mypage.domain.ReadStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.OffsetDateTime

@Entity
@Table(name = "user_books")
class UserBook(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    val book: Book,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var status: ReadStatus = ReadStatus.STOPPED,

    @Column(name = "created_at", nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "finished_at", nullable = true)
    var finishedAt: OffsetDateTime? = null,

    @Column(name = "is_bookmarked", nullable = false)
    var isBookmarked: Boolean = false,

    // 누적 캐시(로그 합산 결과)
    @Column(name = "total_pages_read", nullable = false)
    var totalPagesRead: Int = 0,

    @Column(name = "total_duration_sec", nullable = false)
    var totalDurationSec: Int = 0,

    @Column(name = "progress_percent", nullable = false)
    var progressPercent: Int = 0,

    // 완독 시점 별점
    @Column(name = "rating")
    var rating: Int? = null,
) {
    val bookId: Long get() = this.book.id
}
