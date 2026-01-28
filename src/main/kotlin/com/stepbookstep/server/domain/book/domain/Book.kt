package com.stepbookstep.server.domain.book.domain

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(
    name = "books",
    indexes = [
        Index(name = "idx_isbn13", columnList = "isbn13", unique = true),
        Index(name = "idx_origin", columnList = "origin"),
        Index(name = "idx_genre", columnList = "genre"),
        Index(name = "idx_published_date", columnList = "pub_date"),
        Index(name = "idx_pub_year", columnList = "pub_year"),
        Index(name = "idx_level", columnList = "level")
    ]
)
class Book(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    // ===== 알라딘 API 필드 =====
    @Column(nullable = false, unique = true, length = 13)
    val isbn13: String,

    @Column(nullable = false, length = 300)
    val title: String,

    @Column(nullable = false, length = 300)
    val author: String,

    @Column(nullable = false, length = 200)
    val publisher: String,

    @Column(name = "pub_date", nullable = false)
    val pubDate: LocalDate,

    @Column(name = "pub_year", nullable = false)
    val pubYear: Int,

    @Column(name = "cover_url", nullable = false, length = 500)
    val coverUrl: String,

    @Column(name = "aladin_link", nullable = false, length = 500)
    val aladinLink: String,

    @Column(name = "price_standard", nullable = false)
    val priceStandard: Int,

    @Column(nullable = false, columnDefinition = "TEXT")
    val description: String,

    @Column(name = "item_page", nullable = false)
    val itemPage: Int,

    @Column(name = "category_id", nullable = false)
    val categoryId: Long,

    @Column(name = "genre_id", nullable = true)
    val genreId: Long?,

    @Column(nullable = false)
    val weight: Int = 0,

    // ===== 서비스 고유 필드 =====
    @Column(nullable = false, length = 50)
    val origin: String,

    @Column(nullable = false, length = 50)
    val genre: String,

    @Column(nullable = false)
    val level: Int = 1,

    @Column(nullable = false)
    val score: Int = 0,

    @Enumerated(EnumType.STRING)
    @Column(name = "vocab_level", nullable = false, length = 20)
    val vocabLevel: VocabLevel = VocabLevel.EASY,

    @Column(name = "is_bestseller", nullable = false)
    val isBestseller: Boolean = false,

    // ===== 시스템 필드 =====
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Book) return false
        return isbn13 == other.isbn13
    }

    override fun hashCode(): Int {
        return isbn13.hashCode()
    }
}

enum class VocabLevel {
    EASY,
    NORMAL,
    HARD
}

