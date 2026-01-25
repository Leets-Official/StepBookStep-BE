package com.stepbookstep.server.domain.home.application

import com.stepbookstep.server.domain.book.domain.BookGenre
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
    private val homeCacheService: HomeCacheService
) {

    fun getHome(genreIds: List<Int>?): HomeResponse {
        val genre = when {
            genreIds.isNullOrEmpty() -> BookGenre.entries.random()
            else -> {
                val hasInvalidId = genreIds.any { it < 0 || it >= BookGenre.entries.size }
                if (hasInvalidId) throw CustomException(ErrorCode.INVALID_GENRE_ID)
                val genres = genreIds.map { BookGenre.entries[it] }
                genres.random()
            }
        }

        val genreBooks = homeCacheService.getGenreBooks(genre.displayName).shuffled().take(20)
        val under200Books = homeCacheService.getUnder200Books().shuffled().take(20)
        val bestsellerBooks = homeCacheService.getBestsellerBooks().shuffled().take(20)

        return HomeResponse(
            genreBooks = GenreBooks.of(genre, genreBooks),
            recommendations = Recommendations.of(under200Books, bestsellerBooks)
            // TODO: 독서 통계 부분은 추가 구현할 예정입니다.
        )
    }
}
