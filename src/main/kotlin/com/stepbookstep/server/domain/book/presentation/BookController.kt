package com.stepbookstep.server.domain.book.presentation

import com.stepbookstep.server.domain.book.application.BookQueryService
import com.stepbookstep.server.domain.book.presentation.dto.BookDetailResponse
import com.stepbookstep.server.domain.book.presentation.dto.BookSearchResponse
import com.stepbookstep.server.global.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Book", description = "도서 API")
@RestController
@RequestMapping("/api/v1/books")
class BookController(
    private val bookQueryService: BookQueryService
) {

    @Operation(summary = "도서 상세 조회", description = "도서 ID로 상세 정보를 조회합니다.")
    @GetMapping("/{bookId}")
    fun getBook(
        @Parameter(description = "도서 ID") @PathVariable bookId: Long
    ): ResponseEntity<ApiResponse<BookDetailResponse>> {
        val book = bookQueryService.findById(bookId)
        val response = BookDetailResponse.from(book, isBookmarked = false, myRecord = null)
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
}
