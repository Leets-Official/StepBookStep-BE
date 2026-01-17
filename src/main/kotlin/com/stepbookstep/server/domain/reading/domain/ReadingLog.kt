package com.stepbookstep.server.domain.reading.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.OffsetDateTime

@Entity
@Table(name = "reading_logs")
class ReadingLog(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "book_id", nullable = false)
    val bookId: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "book_status", nullable = false, length = 20)
    val bookStatus: ReadingLogStatus,

    @Column(name = "record_date", nullable = false)
    val recordDate: LocalDate,

    @Column(name = "read_quantity")
    val readQuantity: Int? = null,

    @Column(name = "duration_seconds")
    val durationSeconds: Int? = null,

    @Column(name = "rating")
    val rating: Int? = null,  // 1~5 별점

    @Column(name = "created_at", nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now()
)

enum class ReadingLogStatus {
    READING,
    FINISHED,
    STOPPED,
}