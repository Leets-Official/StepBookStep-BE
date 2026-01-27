package com.stepbookstep.server.domain.book.application

import com.stepbookstep.server.domain.book.domain.Book
import com.stepbookstep.server.domain.book.domain.BookRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class BookCacheService(
    private val bookRepository: BookRepository
) {

    @Cacheable(value = ["bookDetail"], key = "#id")
    fun getBookDetail(id: Long): Book? {
        return bookRepository.findById(id).orElse(null)
    }

    @Cacheable(value = ["booksByLevel"], key = "#level")
    fun getBooksByLevel(level: Int): List<Book> {
        return bookRepository.findAllByLevel(level)
    }
}
