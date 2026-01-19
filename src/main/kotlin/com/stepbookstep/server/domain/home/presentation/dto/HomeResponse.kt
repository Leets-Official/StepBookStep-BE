package com.stepbookstep.server.domain.home.presentation.dto

import com.stepbookstep.server.domain.book.domain.Book
import com.stepbookstep.server.domain.book.domain.BookGenre

data class HomeResponse(
    val genreBooks: GenreBooks,
    val recommendations: Recommendations
    // TODO: 독서 통계 섹션 - 독서 기록 API 연동 후 추가 예정
)

data class GenreBooks(
    val genre: String,
    val genreId: Int,
    val books: List<BookItem>
) {
    companion object {
        fun of(genre: BookGenre, books: List<Book>): GenreBooks {
            return GenreBooks(
                genre = genre.displayName,
                genreId = genre.ordinal,
                books = books.map { BookItem.from(it) }
            )
        }
    }
}

data class Recommendations(
    val under200: List<BookItem>,
    val levelUp: List<BookItem>,
    val bestseller: List<BookItem>
) {
    companion object {
        fun of(
            under200Books: List<Book>,
            bestsellerBooks: List<Book>
        ): Recommendations {
            return Recommendations(
                under200 = under200Books.map { BookItem.from(it) },
                levelUp = emptyList(), // TODO: 레벨업 도전 도서 - API 연동 후 구현 예정
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
