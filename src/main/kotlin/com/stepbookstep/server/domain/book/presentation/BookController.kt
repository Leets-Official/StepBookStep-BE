package com.stepbookstep.server.domain.book.presentation

import com.stepbookstep.server.domain.book.application.BookQueryService
import com.stepbookstep.server.domain.book.presentation.dto.BookDetailResponse
import com.stepbookstep.server.domain.book.presentation.dto.BookFilterResponse
import com.stepbookstep.server.domain.book.presentation.dto.BookSearchResponse
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

    @Operation(summary = "도서 상세 조회", description = "도서 ID로 상세 정보를 조회합니다. 북마크 여부가 포함됩니다.")
    @GetMapping("/{bookId}")
    fun getBook(
        @Parameter(description = "도서 ID") @PathVariable bookId: Long,
        @Parameter(hidden = true) @LoginUserId userId: Long
    ): ResponseEntity<ApiResponse<BookDetailResponse>> {
        val book = bookQueryService.findById(bookId)
        val userBook = userBookRepository.findByUserIdAndBookId(userId, bookId)
        val isBookmarked = userBook?.isBookmarked ?: false

        val response = BookDetailResponse.from(book, isBookmarked = isBookmarked)
        return ResponseEntity.ok(ApiResponse.ok(response))
    }

    @Operation(
        summary = "도서 검색",
        description = """
            키워드로 검색하거나 사용자 등급에 맞는 도서 목록을 조회합니다.

            ## 키워드 입력 시
            - 제목, 저자, 출판사에서 키워드를 검색합니다.

            ## 키워드 미입력 시
            - 사용자의 독서 등급(읽는 중/완독한 도서의 평균 score)을 기반으로 레벨을 결정합니다.
            - score 0~35 → level 1, score 36~65 → level 2, score 66~100 → level 3
            - 해당 레벨의 도서 4권을 랜덤하게 반환합니다.
            - 독서 히스토리가 없으면 level 1 도서를 반환합니다.
        """
    )
    @GetMapping("/search")
    fun searchBooks(
        @Parameter(hidden = true) @LoginUserId userId: Long,
        @Parameter(description = "검색 키워드 (제목, 저자, 출판사)") @RequestParam(required = false) keyword: String?
    ): ResponseEntity<ApiResponse<List<BookSearchResponse>>> {
        val books = bookQueryService.search(userId, keyword)
        val response = books.map { BookSearchResponse.from(it) }
        return ResponseEntity.ok(ApiResponse.ok(response))
    }

    @Operation(
        summary = "도서 필터 검색",
        description = """
            선택한 필터 조건에 맞는 도서 목록을 조회합니다.

            ## 필터 옵션
            - **level**: 난이도 (1, 2, 3)
            - **pageRange**: 분량 (~200, 201~250, 251~350, 351~500, 501~650, 651~) - 중복 선택 가능
            - **origin**: 국가별 (한국소설, 영미소설, 중국소설, 일본소설, 프랑스소설, 독일소설)
            - **genre**: 장르별 (로맨스, 희곡, 무협소설, 판타지/환상문학, 역사소설, 라이트노벨, 추리/미스터리, 과학소설(SF), 액션/스릴러, 호러/공포소설)
            - **keyword**: 검색어 (제목, 저자, 출판사에서 검색) - 필터 선택 후 사용 가능

            ## 페이지네이션 
            - **cursor**: 마지막으로 조회한 bookId (첫 요청 시 생략)
            - **size**: 조회할 개수 (고정값 20)
            - **hasNext**: 다음 페이지 존재 여부
            - 정렬: id 오름차순

            모든 필터는 선택 사항이며, 복수 필터 적용 시 AND 조건으로 검색됩니다.
            pageRange는 중복 선택 시 OR 조건으로 검색됩니다.
            keyword 입력 시 필터링된 결과 내에서 추가로 검색됩니다.
            유효하지 않은 필터 값을 입력하면 400 Bad Request 에러가 반환됩니다.
        """
    )
    @GetMapping("/filter")
    fun filterBooks(
        @Parameter(description = "난이도") @RequestParam(required = false) level: Int?,
        @Parameter(description = "분량 (중복 선택 가능)") @RequestParam(required = false) pageRange: List<String>?,
        @Parameter(description = "국가별 분류") @RequestParam(required = false) origin: String?,
        @Parameter(description = "장르별 분류") @RequestParam(required = false) genre: String?,
        @Parameter(description = "검색어 (제목, 저자, 출판사)") @RequestParam(required = false) keyword: String?,
        @Parameter(description = "마지막으로 조회한 bookId (첫 요청 시 생략)") @RequestParam(required = false) cursor: Long?,
        @Parameter(description = "조회할 개수") @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<BookFilterResponse>> {
        val response = bookQueryService.filter(level, pageRange, origin, genre, keyword, cursor, size)
        return ResponseEntity.ok(ApiResponse.ok(response))
    }
}
