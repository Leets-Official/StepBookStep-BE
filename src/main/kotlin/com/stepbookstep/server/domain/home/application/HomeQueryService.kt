package com.stepbookstep.server.domain.home.application

import com.stepbookstep.server.domain.book.domain.Book
import com.stepbookstep.server.domain.book.domain.BookRepository
import com.stepbookstep.server.domain.home.presentation.dto.GenreBooks
import com.stepbookstep.server.domain.home.presentation.dto.HomeResponse
import com.stepbookstep.server.domain.home.presentation.dto.ReadingStatistics
import com.stepbookstep.server.domain.home.presentation.dto.Recommendations
import com.stepbookstep.server.domain.reading.application.StatisticsService
import com.stepbookstep.server.domain.reading.domain.UserBookRepository
import com.stepbookstep.server.domain.user.domain.UserCategoryPreferenceRepository
import com.stepbookstep.server.domain.user.domain.UserGenrePreferenceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Year

@Service
@Transactional(readOnly = true)
class HomeQueryService(
    private val homeCacheService: HomeCacheService,
    private val userCategoryPreferenceRepository: UserCategoryPreferenceRepository,
    private val userGenrePreferenceRepository: UserGenrePreferenceRepository,
    private val userBookRepository: UserBookRepository,
    private val bookRepository: BookRepository,
    private val statisticsService: StatisticsService
) {

    fun getHome(userId: Long): HomeResponse {
        val readingStatistics = getReadingStatistics(userId)
        val selectedBooks = selectGenreBooksForUser(userId)

        val (lightReadsBooks, levelUpBooks) = getPersonalizedRecommendations(userId)
        val bestsellerBooks = homeCacheService.getBestsellerBooks().shuffled().take(20)

        return HomeResponse(
            readingStatistics = readingStatistics,
            genreBooks = GenreBooks.of(
                type = selectedBooks.type,
                id = selectedBooks.id,
                name = selectedBooks.name,
                books = selectedBooks.books
            ),
            recommendations = Recommendations.of(lightReadsBooks, levelUpBooks, bestsellerBooks)
        )
    }

    private fun getReadingStatistics(userId: Long): ReadingStatistics {
        val currentYear = Year.now().value
        val stats = statisticsService.getReadingStatistics(userId, currentYear)

        val favoriteCategory = stats.categoryPreference.categories
            .firstOrNull()?.categoryName

        return ReadingStatistics(
            finishedBookCount = stats.bookSummary.finishedBookCount,
            cumulativeHours = stats.cumulativeTime.hours,
            achievementRate = stats.goalAchievement.achievementRate,
            favoriteCategory = favoriteCategory
        )
    }

    private fun selectGenreBooksForUser(userId: Long): SelectedBooks {
        val categoryPreferences = userCategoryPreferenceRepository.findAllByUserId(userId)
        val genrePreferences = userGenrePreferenceRepository.findAllByUserId(userId)

        val allPreferences = mutableListOf<Pair<String, Long>>()

        categoryPreferences.forEach { pref ->
            allPreferences.add("category" to pref.categoryId)
        }
        genrePreferences.forEach { pref ->
            allPreferences.add("genre" to pref.genreId)
        }

        // 선택한 preference가 없으면 랜덤으로 category 또는 genre 선택
        if (allPreferences.isEmpty()) {
            return selectRandomCategoryOrGenre()
        }

        val shuffledPreferences = allPreferences.shuffled()
        for (preference in shuffledPreferences) {
            val books = when (preference.first) {
                "category" -> homeCacheService.getBooksByCategoryId(preference.second)
                "genre" -> homeCacheService.getBooksByGenreId(preference.second)
                else -> emptyList()
            }.shuffled().take(20)

            if (books.isNotEmpty()) {
                val firstBook = books.first()
                val name = when (preference.first) {
                    "category" -> firstBook.origin
                    "genre" -> firstBook.genre
                    else -> ""
                }
                return SelectedBooks(preference.first, preference.second, name, books)
            }
        }

        // 선택한 preference에 매칭되는 책이 없으면 랜덤 선택
        return selectRandomCategoryOrGenre()
    }

    private fun selectRandomCategoryOrGenre(): SelectedBooks {
        // 캐시된 DISTINCT ID 목록 조회
        val categoryIds = homeCacheService.getDistinctCategoryIds()
        val genreIds = homeCacheService.getDistinctGenreIds()

        val allOptions = mutableListOf<Pair<String, Long>>()
        categoryIds.forEach { allOptions.add("category" to it) }
        genreIds.forEach { allOptions.add("genre" to it) }

        if (allOptions.isEmpty()) {
            return SelectedBooks("none", 0L, "추천도서", emptyList())
        }

        val selected = allOptions.random()
        val books = when (selected.first) {
            "category" -> homeCacheService.getBooksByCategoryId(selected.second)
            "genre" -> homeCacheService.getBooksByGenreId(selected.second)
            else -> emptyList()
        }.shuffled().take(20)

        if (books.isEmpty()) {
            return SelectedBooks("none", 0L, "추천도서", emptyList())
        }

        val firstBook = books.first()
        val name = when (selected.first) {
            "category" -> firstBook.origin
            "genre" -> firstBook.genre
            else -> ""
        }
        return SelectedBooks(selected.first, selected.second, name, books)
    }

    private data class SelectedBooks(
        val type: String,
        val id: Long,
        val name: String,
        val books: List<Book>
    )

    private fun getPersonalizedRecommendations(userId: Long): Pair<List<Book>, List<Book>> {
        val userBooks = userBookRepository.findReadingAndFinishedBooksByUserId(userId)

        return if (userBooks.isEmpty()) {
            val lightReads = homeCacheService.getUnder200Books().shuffled().take(20)
            val levelUp = homeCacheService.getLevel3Books().shuffled().take(20)
            Pair(lightReads, levelUp)
        } else {
            val avgPageCount = userBooks.map { it.book.itemPage }.average().toInt()
            val avgScore = userBooks.map { it.book.score }.average().toInt()

            val lightReads = bookRepository.findAllByPageRange(avgPageCount - 10, avgPageCount + 10)
                .shuffled().take(20)
            val levelUp = bookRepository.findAllByScoreRange(avgScore + 15, avgScore + 20)
                .shuffled().take(20)

            Pair(lightReads, levelUp)
        }
    }
}
