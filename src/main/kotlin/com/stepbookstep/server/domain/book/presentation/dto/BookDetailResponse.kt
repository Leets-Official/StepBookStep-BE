package com.stepbookstep.server.domain.book.presentation.dto

import com.stepbookstep.server.domain.book.domain.Book

data class BookDetailResponse(
    val bookInfo: BookInfo,
    val isBookmarked: Boolean,
    val myRecord: MyRecord?
) {
    companion object {
        fun from(book: Book, isBookmarked: Boolean = false, myRecord: MyRecord? = null): BookDetailResponse {
            return BookDetailResponse(
                bookInfo = BookInfo.from(book),
                isBookmarked = isBookmarked,
                myRecord = myRecord
            )
        }
    }
}

data class BookInfo(
    val bookId: Long,
    val coverImage: String,
    val title: String,
    val author: String,
    val publisher: String,
    val pubDate: String,
    val totalPage: Int,
    val priceStandard: Int,
    val link: String,
    val description: String,
    val tags: List<String>
) {
    companion object {
        fun from(book: Book): BookInfo {
            return BookInfo(
                bookId = book.id,
                coverImage = book.coverUrl,
                title = book.title,
                author = book.author,
                publisher = book.publisher,
                pubDate = book.pubYear.toString(),
                totalPage = book.itemPage,
                priceStandard = book.priceStandard,
                link = book.aladinLink,
                description = book.description,
                tags = BookTagBuilder.buildTags(book)
            )
        }
    }
}

object BookTagBuilder {
    fun buildTags(book: Book): List<String> {
        return buildList {
            add(getPageRangeTag(book.itemPage))
            add(book.origin)
            if (!book.genre.isNullOrBlank()) {
                add(book.genre)
            }
        }
    }

    private fun getPageRangeTag(page: Int): String {
        return when {
            page <= 200 -> "~200"
            page <= 250 -> "201~250"
            page <= 350 -> "251~350"
            page <= 500 -> "351~500"
            page <= 650 -> "501~650"
            else -> "651~"
        }
    }
}

data class MyRecord(
    val status: String,
    val startDate: String?,
    val endDate: String?,
    val currentPage: Int,
    val readPercent: Int
)
