package com.stepbookstep.server.domain.book.domain

import org.springframework.data.jpa.domain.Specification

object BookSpecification {

    /**
     * 난이도 필터 (1, 2, 3)
     */
    fun withLevel(level: Int?): Specification<Book> {
        return Specification { root, _, cb ->
            if (level == null) {
                null
            } else {
                cb.equal(root.get<Int>("level"), level)
            }
        }
    }

    /**
     * 분량 필터 (단일 선택, 선택한 값 이하 모두 포함)
     * ~200 → itemPage <= 200
     * 250 → itemPage <= 250
     * 350 → itemPage <= 350
     * 500 → itemPage <= 500
     * 650 → itemPage <= 650
     * 651~ → 분량 제한 없음 (전체)
     */
    fun withPageRange(pageRange: String?): Specification<Book> {
        return Specification { root, _, cb ->
            if (pageRange.isNullOrBlank()) {
                null
            } else {
                when (pageRange) {
                    "~200" -> cb.lessThanOrEqualTo(root.get<Int>("itemPage"), 200)
                    "250" -> cb.lessThanOrEqualTo(root.get<Int>("itemPage"), 250)
                    "350" -> cb.lessThanOrEqualTo(root.get<Int>("itemPage"), 350)
                    "500" -> cb.lessThanOrEqualTo(root.get<Int>("itemPage"), 500)
                    "650" -> cb.lessThanOrEqualTo(root.get<Int>("itemPage"), 650)
                    "651~" -> null // 전체 분량
                    else -> null
                }
            }
        }
    }

    /**
     * 국가별 분류 필터
     */
    fun withOrigin(origin: String?): Specification<Book> {
        return Specification { root, _, cb ->
            if (origin.isNullOrBlank()) {
                null
            } else {
                cb.equal(root.get<String>("origin"), origin)
            }
        }
    }

    /**
     * 장르별 분류 필터
     */
    fun withGenre(genre: String?): Specification<Book> {
        return Specification { root, _, cb ->
            if (genre.isNullOrBlank()) {
                null
            } else {
                cb.equal(root.get<String>("genre"), genre)
            }
        }
    }

    /**
     * 키워드 검색 필터 (제목, 저자, 출판사)
     * 공백으로 구분된 각 키워드가 title/author/publisher 중 하나에 포함되면 검색됨
     */
    fun withKeyword(keyword: String?): Specification<Book> {
        return Specification { root, _, cb ->
            if (keyword.isNullOrBlank()) {
                null
            } else {
                val keywords = keyword.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
                if (keywords.isEmpty()) {
                    null
                } else {
                    val keywordPredicates = keywords.map { word ->
                        val pattern = "%$word%"
                        cb.or(
                            cb.like(root.get("title"), pattern),
                            cb.like(root.get("author"), pattern),
                            cb.like(root.get("publisher"), pattern)
                        )
                    }
                    cb.and(*keywordPredicates.toTypedArray())
                }
            }
        }
    }

    /**
     * 커서 기반 페이지네이션 (id > cursor)
     */
    fun withCursor(cursor: Long?): Specification<Book> {
        return Specification { root, _, cb ->
            if (cursor == null) {
                null
            } else {
                cb.greaterThan(root.get("id"), cursor)
            }
        }
    }
}
