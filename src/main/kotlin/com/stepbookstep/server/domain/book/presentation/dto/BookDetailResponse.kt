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
                tags = buildTags(book)
            )
        }

        // TODO: 태그 키워드가 정해지면 리팩토링할 예정입니다.
        private fun buildTags(book: Book): List<String> {
            return listOf(
                "Lv.${book.level}",
                "#${book.itemPage}p",
                "#${book.vocabLevel.toDisplayName()}",
                "#${book.genre.displayName}"
            )
        }

        private fun com.stepbookstep.server.domain.book.domain.VocabLevel.toDisplayName(): String {
            return when (this) {
                com.stepbookstep.server.domain.book.domain.VocabLevel.EASY -> "쉬운어휘"
                com.stepbookstep.server.domain.book.domain.VocabLevel.NORMAL -> "보통어휘"
                com.stepbookstep.server.domain.book.domain.VocabLevel.HARD -> "어려운어휘"
            }
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
