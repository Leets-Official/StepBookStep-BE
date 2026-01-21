package com.stepbookstep.server.domain.mypage.application.dto

import com.stepbookstep.server.domain.mypage.domain.ReadStatus
import com.stepbookstep.server.domain.reading.domain.UserBook
import org.springframework.data.domain.Page
import java.time.OffsetDateTime

data class MyBooksResponse(
    val items: List<MyBookItem>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
) {
    companion object {
        fun from(pageResult: Page<MyBookItem>): MyBooksResponse {
            return MyBooksResponse(
                items = pageResult.content,
                page = pageResult.number,
                size = pageResult.size,
                totalElements = pageResult.totalElements,
                totalPages = pageResult.totalPages
            )
        }
    }
}

data class MyBookItem(
    val bookId: Long,
    val userBookId: Long,
    val title: String,
    val author: String,
    val coverUrl: String,
    val status: ReadStatus,
    val isBookmarked: Boolean,
    val createdAt: OffsetDateTime?,
    val finishedAt: OffsetDateTime?,
    val totalPagesRead: Int,
    val progressPercent: Int,
    val rating: Int?,
    val updatedAt: OffsetDateTime
) {
    companion object {
        fun from(ub: UserBook): MyBookItem {
            return MyBookItem(
                bookId = ub.bookId,
                userBookId = ub.id,
                title = ub.book.title,
                author = ub.book.author,
                coverUrl = ub.book.coverUrl,
                status = ub.status,
                isBookmarked = ub.isBookmarked,
                createdAt = ub.createdAt,
                finishedAt = ub.finishedAt,
                totalPagesRead = ub.totalPagesRead,
                progressPercent = ub.progressPercent,
                rating = ub.rating,
                updatedAt = ub.updatedAt
            )
        }
    }
}