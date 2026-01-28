package com.stepbookstep.server.domain.home.application

import com.stepbookstep.server.domain.book.domain.Book
import com.stepbookstep.server.domain.book.domain.BookRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class HomeCacheService(
    private val bookRepository: BookRepository
) {

    @Cacheable(value = ["genreBooks"], key = "#genre")
    fun getGenreBooks(genre: String): List<Book> {
        return bookRepository.findAllByGenre(genre)
    }

    @Cacheable(value = ["under200Books"])
    fun getUnder200Books(): List<Book> {
        return bookRepository.findAllUnder200Pages()
    }

    @Cacheable(value = ["bestsellerBooks"])
    fun getBestsellerBooks(): List<Book> {
        return bookRepository.findAllBestsellers()
    }

    @Cacheable(value = ["level3Books"])
    fun getLevel3Books(): List<Book> {
        return bookRepository.findAllByLevel3()
    }

    @Cacheable(value = ["categoryBooks"], key = "#categoryId")
    fun getBooksByCategoryId(categoryId: Long): List<Book> {
        return bookRepository.findAllByCategoryId(categoryId)
    }

    @Cacheable(value = ["genreIdBooks"], key = "#genreId")
    fun getBooksByGenreId(genreId: Long): List<Book> {
        return bookRepository.findAllByGenreId(genreId)
    }
}
