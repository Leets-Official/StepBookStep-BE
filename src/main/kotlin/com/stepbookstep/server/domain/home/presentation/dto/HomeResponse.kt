package com.stepbookstep.server.domain.home.presentation.dto

import com.stepbookstep.server.domain.book.domain.Book

data class HomeResponse(
    val readingStatistics: ReadingStatistics,
    val genreBooks: GenreBooks,
    val recommendations: Recommendations
)

data class ReadingStatistics(
    val finishedBookCount: Int,
    val cumulativeHours: Int,
    val achievementRate: Int,
    val favoriteCategory: String?
)

data class GenreBooks(
    val type: String,
    val id: Long,
    val name: String,
    val books: List<BookItem>
) {
    companion object {
        fun of(type: String, id: Long, name: String, books: List<Book>): GenreBooks {
            return GenreBooks(
                type = type,
                id = id,
                name = name,
                books = books.map { BookItem.from(it) }
            )
        }
    }
}

data class Recommendations(
    val lightReads: List<BookItem>,
    val levelUp: List<BookItem>,
    val bestseller: List<BookItem>
) {
    companion object {
        fun of(
            lightReadsBooks: List<Book>,
            levelUpBooks: List<Book>,
            bestsellerBooks: List<Book>
        ): Recommendations {
            return Recommendations(
                lightReads = lightReadsBooks.map { BookItem.from(it) },
                levelUp = levelUpBooks.map { BookItem.from(it) },
                bestseller = bestsellerBooks.map { BookItem.from(it) }
            )
        }
    }
}

data class BookItem(
    val bookId: Long,
    val title: String,
    val coverImage: String
) {
    companion object {
        fun from(book: Book): BookItem {
            return BookItem(
                bookId = book.id,
                title = book.title,
                coverImage = book.coverUrl
            )
        }
    }
}
