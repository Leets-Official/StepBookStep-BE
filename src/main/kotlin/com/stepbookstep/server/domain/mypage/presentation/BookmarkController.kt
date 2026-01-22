package com.stepbookstep.server.domain.mypage.presentation

import com.stepbookstep.server.domain.mypage.application.BookmarkService
import com.stepbookstep.server.global.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@Tag(name = "Bookmark", description = "도서 북마크 API")
@RestController
@RequestMapping("/api/v1/books")
class BookmarkController(
    private val bookmarkService: BookmarkService
) {

    @Operation(summary = "도서 북마크 등록", description = "도서를 '읽고 싶은 책(북마크)'으로 등록합니다.")
    @PutMapping("/{bookId}/bookmark")
    fun addBookmark(
        @AuthenticationPrincipal userId: Long,
        @PathVariable bookId: Long
    ) : ResponseEntity<ApiResponse<BookmarkResponse>> {
        bookmarkService.addBookmark(userId, bookId)
        val response = BookmarkResponse(bookId = bookId, bookmarked = true)
        return ResponseEntity.ok(ApiResponse.ok(response)
        )
    }

    @Operation(summary = "도서 북마크 해제", description = "도서의 북마크를 해제합니다.")
    @DeleteMapping("/{bookId}/bookmark")
    fun removeBookmark(
        @AuthenticationPrincipal userId: Long,
        @PathVariable bookId: Long
    ) : ResponseEntity<ApiResponse<BookmarkResponse>> {
        bookmarkService.removeBookmark(userId, bookId)
        val response = BookmarkResponse(bookId = bookId, bookmarked = false)
        return ResponseEntity.ok(
            ApiResponse.ok(response)
        )
    }
}

data class BookmarkResponse(
    val bookId: Long,
    val bookmarked: Boolean
)