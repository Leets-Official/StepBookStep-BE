package com.stepbookstep.server.domain.book.presentation.dto

import com.stepbookstep.server.domain.book.domain.Book

data class BookFilterResponse(
    val books: List<BookFilterItem>,
    val hasNext: Boolean
) {
    companion object {
        fun of(books: List<Book>, hasNext: Boolean): BookFilterResponse {
            return BookFilterResponse(
                books = books.map { BookFilterItem.from(it) },
                hasNext = hasNext
            )
        }
    }
}

data class BookFilterItem(
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
        fun from(book: Book): BookFilterItem {
            val tags = mutableListOf<String>()

            // 분량 태그
            when {
                book.itemPage <= 200 -> tags.add("~200쪽")
                book.itemPage <= 250 -> tags.add("201~250쪽")
                book.itemPage <= 350 -> tags.add("251~350쪽")
                book.itemPage <= 500 -> tags.add("351~500쪽")
                book.itemPage <= 650 -> tags.add("501~650쪽")
                else -> tags.add("651~")
            }

            // 국가 태그
            tags.add(book.origin)

            // 장르 태그
            tags.add(book.genre)

            return BookFilterItem(
                bookId = book.id,
                coverImage = book.coverUrl,
                title = book.title,
                author = book.author,
                publisher = book.publisher,
                pubDate = book.pubYear.toString(),
                totalPage = book.itemPage,
                tags = tags
            )
        }
    }
}
