package com.stepbookstep.server.domain.book.application

import com.stepbookstep.server.domain.book.domain.Book
import com.stepbookstep.server.domain.book.domain.BookRepository
import com.stepbookstep.server.domain.book.domain.BookSpecification
import com.stepbookstep.server.domain.book.presentation.dto.BookFilterResponse
import com.stepbookstep.server.domain.reading.domain.UserBookRepository
import com.stepbookstep.server.global.response.CustomException
import com.stepbookstep.server.global.response.ErrorCode
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class BookQueryService(
    private val bookRepository: BookRepository,
    private val bookCacheService: BookCacheService,
    private val userBookRepository: UserBookRepository
) {

    companion object {
        private val logger = LoggerFactory.getLogger(BookQueryService::class.java)
        private const val PAGE_SIZE = 20
        private val VALID_LEVELS = setOf(1, 2, 3)
        private val VALID_PAGE_RANGES = setOf("~200", "250", "350", "500", "650", "651~")
        private val VALID_ORIGINS = setOf("한국소설", "영미소설", "중국소설", "일본소설", "프랑스소설", "독일소설")
        private val VALID_GENRES = setOf(
            "로맨스", "희곡", "무협소설", "판타지/환상문학", "역사소설",
            "라이트노벨", "추리/미스터리", "과학소설(SF)", "액션/스릴러", "호러/공포소설"
        )
    }

    fun findById(id: Long): Book {
        return bookCacheService.getBookDetail(id)
            ?: throw CustomException(ErrorCode.BOOK_NOT_FOUND, null)
    }

    fun search(userId: Long, keyword: String?): List<Book> {
        return if (keyword.isNullOrBlank()) {
            val userLevel = calculateUserLevel(userId)
            bookCacheService.getBooksByLevel(userLevel).shuffled().take(4)
        } else {
            val results = bookRepository.searchByKeyword(keyword)
            if (results.isEmpty()) {
                throw CustomException(ErrorCode.NOT_SEARCH, null)
            }
            results
        }
    }

    private fun calculateUserLevel(userId: Long): Int {
        val userBooks = userBookRepository.findReadingAndFinishedBooksByUserId(userId)

        if (userBooks.isEmpty()) {
            logger.info("[BookSearch] userId={}, 독서 히스토리 없음 → level=1", userId)
            return 1
        }

        val avgScore = userBooks.map { it.book.score }.average().toInt()
        val level = when {
            avgScore <= 35 -> 1
            avgScore <= 65 -> 2
            else -> 3
        }

        logger.info("[BookSearch] userId={}, avgScore={}, level={}", userId, avgScore, level)
        return level
    }

    fun filter(
        level: Int?,
        pageRange: String?,
        origin: String?,
        genre: String?,
        keyword: String?,
        cursor: Long?
    ): BookFilterResponse {
        // 필터 없이 검색어만 입력한 경우 예외 처리
        val hasFilter = level != null || pageRange != null || origin != null || genre != null
        if (!hasFilter && !keyword.isNullOrBlank()) {
            throw CustomException(ErrorCode.FILTER_REQUIRED, null)
        }

        // 유효성 검증
        validateFilterParams(level, pageRange, origin, genre)

        val spec = Specification.where(BookSpecification.withLevel(level))
            .and(BookSpecification.withPageRange(pageRange))
            .and(BookSpecification.withOrigin(origin))
            .and(BookSpecification.withGenre(genre))
            .and(BookSpecification.withKeyword(keyword))
            .and(BookSpecification.withCursor(cursor))

        // PAGE_SIZE + 1개 조회하여 다음 페이지 존재 여부 확인
        val pageable = PageRequest.of(0, PAGE_SIZE + 1, Sort.by(Sort.Direction.ASC, "id"))
        val result = bookRepository.findAll(spec, pageable).content

        val hasNext = result.size > PAGE_SIZE
        val books = if (hasNext) result.dropLast(1) else result

        return BookFilterResponse.of(
            books = books,
            hasNext = hasNext
        )
    }

    private fun validateFilterParams(
        level: Int?,
        pageRange: String?,
        origin: String?,
        genre: String?
    ) {
        if (level != null && level !in VALID_LEVELS) {
            throw CustomException(ErrorCode.INVALID_DIFFICULTY, null)
        }
        if (pageRange != null && pageRange !in VALID_PAGE_RANGES) {
            throw CustomException(ErrorCode.INVALID_PAGE_RANGE, null)
        }
        if (origin != null && origin !in VALID_ORIGINS) {
            throw CustomException(ErrorCode.INVALID_ORIGIN, null)
        }
        if (genre != null && genre !in VALID_GENRES) {
            throw CustomException(ErrorCode.INVALID_GENRE, null)
        }
    }
}
