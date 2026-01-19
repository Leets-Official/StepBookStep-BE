package com.stepbookstep.server.domain.book.application

import com.stepbookstep.server.domain.book.domain.Book
import com.stepbookstep.server.domain.book.domain.BookRepository
import com.stepbookstep.server.global.response.CustomException
import com.stepbookstep.server.global.response.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class BookQueryService(
    private val bookRepository: BookRepository,
    private val bookCacheService: BookCacheService
) {

    fun findById(id: Long): Book {
        return bookCacheService.getBookDetail(id)
            ?: throw CustomException(ErrorCode.BOOK_NOT_FOUND, null)
    }

    fun search(keyword: String?, level: Int): List<Book> {
        return if (keyword.isNullOrBlank()) {
            bookRepository.findRandomByLevel(level)
        } else {
            val results = bookRepository.searchByKeyword(keyword)
            if (results.isEmpty()) {
                throw CustomException(ErrorCode.NOT_SEARCH, null)
            }
            results
        }
    }
}
