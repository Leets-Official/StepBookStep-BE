package com.stepbookstep.server.domain.mypage.application

import com.stepbookstep.server.domain.mypage.application.dto.MyBookItem
import com.stepbookstep.server.domain.mypage.domain.MyShelf
import com.stepbookstep.server.domain.mypage.domain.ReadStatus
import com.stepbookstep.server.domain.mypage.domain.MyPageUserBookRepository
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.data.domain.Pageable

/**
 * - 내 서재 목록 조회 전용 서비스
 * - readStatus 를 보고 "상태 기반 조회" 또는 "북마크 기반 조회"를 수행
 * - 기본: 최근 기록순
 */
@Service
class MyBookQueryService(
    private val myPageUserBookRepository: MyPageUserBookRepository
) {

    @Transactional(readOnly = true)
    fun getMyBooks(
        userId: Long,
        tab: MyShelf,
        pageable: Pageable
    ): Page<MyBookItem> {

        val result = when (tab) {
            MyShelf.BOOKMARKED ->
                myPageUserBookRepository.findByUserIdAndIsBookmarkedTrue(userId, pageable)

            MyShelf.READING ->
                myPageUserBookRepository.findByUserIdAndStatus(userId, ReadStatus.READING, pageable)

            MyShelf.FINISHED ->
                myPageUserBookRepository.findByUserIdAndStatus(userId, ReadStatus.FINISHED, pageable)

            MyShelf.STOPPED ->
                myPageUserBookRepository.findByUserIdAndStatus(userId, ReadStatus.STOPPED, pageable)
        }

        return result.map { MyBookItem.from(it) }
    }
}