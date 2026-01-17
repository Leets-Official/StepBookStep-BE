package com.stepbookstep.server.domain.home.application

import com.stepbookstep.server.domain.book.domain.BookGenre
import com.stepbookstep.server.domain.book.domain.BookRepository
import com.stepbookstep.server.domain.home.presentation.dto.GenreBooks
import com.stepbookstep.server.domain.home.presentation.dto.HomeResponse
import com.stepbookstep.server.domain.home.presentation.dto.Recommendations
import com.stepbookstep.server.global.response.CustomException
import com.stepbookstep.server.global.response.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class HomeQueryService(
    private val bookRepository: BookRepository
) {

    fun getHome(genreId: Int?): HomeResponse {
        val genre = genreId?.let {
            BookGenre.entries.getOrNull(it) ?: throw CustomException(ErrorCode.INVALID_GENRE_ID)
        } ?: BookGenre.entries.first()

        val genreBooks = bookRepository.findRandomByGenre(genre.displayName)
        val under200Books = bookRepository.findUnder200Pages()
        val bestsellerBooks = bookRepository.findBestsellers()

        return HomeResponse(
            genreBooks = GenreBooks.of(genre, genreBooks),
            recommendations = Recommendations.of(under200Books, bestsellerBooks)
        )
    }
}
