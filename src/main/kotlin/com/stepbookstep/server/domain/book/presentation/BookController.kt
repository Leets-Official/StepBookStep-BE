package com.stepbookstep.server.domain.book.presentation

import com.stepbookstep.server.domain.book.application.BookQueryService
import com.stepbookstep.server.domain.book.presentation.dto.BookDetailResponse
import com.stepbookstep.server.domain.book.presentation.dto.BookFilterResponse
import com.stepbookstep.server.domain.book.presentation.dto.BookSearchResponse
import com.stepbookstep.server.domain.book.presentation.dto.MyRecord
import com.stepbookstep.server.domain.reading.domain.UserBookRepository
import com.stepbookstep.server.global.response.ApiResponse
import com.stepbookstep.server.security.jwt.LoginUserId
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Book", description = "도서 API")
@RestController
@RequestMapping("/api/v1/books")
class BookController(
    private val bookQueryService: BookQueryService,
    private val userBookRepository: UserBookRepository
) {

    @Operation(summary = "도서 상세 조회", description = "도서 ID로 상세 정보를 조회합니다. 북마크 여부와 독서 기록이 포함됩니다.")
    @GetMapping("/{bookId}")
    fun getBook(
        @Parameter(description = "도서 ID") @PathVariable bookId: Long,
        @Parameter(hidden = true) @LoginUserId userId: Long
    ): ResponseEntity<ApiResponse<BookDetailResponse>> {
        val book = bookQueryService.findById(bookId)

        val userBook = userBookRepository.findByUserIdAndBookId(userId, bookId)

        val isBookmarked = userBook?.isBookmarked ?: false
        val myRecord = userBook?.let {
            MyRecord(
                status = it.status.name,
                startDate = it.createdAt.toLocalDate().toString(),
                endDate = it.finishedAt?.toLocalDate()?.toString(),
                currentPage = it.totalPagesRead,
                readPercent = it.progressPercent
            )
        }

        val response = BookDetailResponse.from(book, isBookmarked = isBookmarked, myRecord = myRecord)
        return ResponseEntity.ok(ApiResponse.ok(response))
    }

    @Operation(summary = "도서 검색", description = "키워드로 검색하거나 레벨별 도서 목록을 조회합니다.")
    @GetMapping("/search")
    fun searchBooks(
        @Parameter(description = "검색 키워드 (제목, 저자, 출판사)") @RequestParam(required = false) keyword: String?,
        @Parameter(description = "사용자 레벨") @RequestParam level: Int
    ): ResponseEntity<ApiResponse<List<BookSearchResponse>>> {
        val books = bookQueryService.search(keyword, level)
        val response = books.map { BookSearchResponse.from(it) }
        return ResponseEntity.ok(ApiResponse.ok(response))
    }

    @Operation(
        summary = "도서 필터 검색",
        description = """
            선택한 필터 조건에 맞는 도서 목록을 조회합니다. (최대 20권씩 페이징)

            ## 필터 옵션
            - **difficulty**: 난이도 (1, 2, 3)
            - **pageRange**: 분량 (~200, 201~250, 251~)
            - **origin**: 국가별 (한국소설, 영미소설, 중국소설, 일본소설, 프랑스소설, 독일소설)
            - **genre**: 장르별 (로맨스, 희곡, 무협소설, 판타지/환상문학, 역사소설, 라이트노벨, 추리/미스터리, 과학소설(SF), 액션/스릴러, 호러/공포소설)

            모든 필터는 선택 사항이며, 복수 필터 적용 시 AND 조건으로 검색됩니다.
            유효하지 않은 필터 값을 입력하면 400 Bad Request 에러가 반환됩니다.
        """
    )
    @GetMapping("/filter")
    fun filterBooks(
        @Parameter(description = "난이도") @RequestParam(required = false) difficulty: Int?,
        @Parameter(description = "분량") @RequestParam(required = false) pageRange: String?,
        @Parameter(description = "국가별 분류") @RequestParam(required = false) origin: String?,
        @Parameter(description = "장르별 분류") @RequestParam(required = false) genre: String?
    ): ResponseEntity<ApiResponse<BookFilterResponse>> {
        val response = bookQueryService.filter(difficulty, pageRange, origin, genre)
        return ResponseEntity.ok(ApiResponse.ok(response))
    }
}
