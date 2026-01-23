package com.stepbookstep.server.domain.mypage.presentation

import com.stepbookstep.server.domain.mypage.application.MyBookQueryService
import com.stepbookstep.server.domain.mypage.application.dto.MyBooksResponse
import com.stepbookstep.server.domain.mypage.domain.MyShelf
import com.stepbookstep.server.security.jwt.LoginUserId
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.*

@Tag(name = "MyPage", description = "마이페이지(내 서재) API")
@RestController
@RequestMapping("/api/v1/my")
class MyBookController(
    private val myBookQueryService: MyBookQueryService
) {

    @Operation(summary = "내 서재 목록 조회", description = """
        사용자의 서재 목록을 readStatus 기준으로 조회합니다.
        
        readStatus 종류
        - READING: 읽는 중인 책
        - FINISHED: 완독한 책
        - PAUSED: 중단한 책
        - BOOKMARKED: 읽고 싶은 책(북마크)
        
        기본 정렬: 최근 기록순(updatedAt DESC)
    """
    )
    @GetMapping("/books")
    fun getMyBooks(
        @Parameter(
            description = "서재 탭",
            example = "READING",
            schema = Schema(
                allowableValues = ["READING", "FINISHED", "PAUSED", "BOOKMARKED"]
            )
        )
        @RequestParam readStatus: String,

        @PageableDefault(size = 20, sort = ["updatedAt"], direction = Sort.Direction.DESC)
        pageable: Pageable,

        @Parameter(hidden = true)
        @LoginUserId userId: Long
    ): MyBooksResponse {
        val tab = MyShelf.from(readStatus)
        val result = myBookQueryService.getMyBooks(userId, tab, pageable)
        return MyBooksResponse.from(result)
    }
}