package com.stepbookstep.server.domain.book.presentation.dto

import com.stepbookstep.server.domain.book.domain.Book

data class BookSearchResponse(
    val bookId: Long,
    val coverImage: String,
    val title: String,
    val author: String,
    val publisher: String,
    val pubDate: String,
    val totalPage: Int,
    val tags: List<String>
) {
    companion object {
        fun from(book: Book): BookSearchResponse {
            return BookSearchResponse(
                bookId = book.id,
                coverImage = book.coverUrl,
                title = book.title,
                author = book.author,
                publisher = book.publisher,
                pubDate = book.pubYear.toString(),
                totalPage = book.itemPage,
                tags = emptyList() // 태그 키워드가 정해지면 추가할 예정입니다.
            )
        }
    }
}
