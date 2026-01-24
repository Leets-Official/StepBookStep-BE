package com.stepbookstep.server.domain.book.domain

import org.springframework.data.jpa.domain.Specification

object BookSpecification {

    /**
     * 난이도 필터 (1, 2, 3)
     */
    fun withDifficulty(difficulty: Int?): Specification<Book> {
        return Specification { root, _, cb ->
            if (difficulty == null) {
                null
            } else {
                cb.equal(root.get<Int>("level"), difficulty)
            }
        }
    }

    /**
     * 분량 필터 (~200, 201~250, 251~)
     */
    fun withPageRange(pageRange: String?): Specification<Book> {
        return Specification { root, _, cb ->
            if (pageRange.isNullOrBlank()) {
                null
            } else {
                when (pageRange) {
                    "~200" -> cb.lessThanOrEqualTo(root.get("itemPage"), 200)
                    "201~250" -> cb.and(
                        cb.greaterThan(root.get("itemPage"), 200),
                        cb.lessThanOrEqualTo(root.get("itemPage"), 250)
                    )
                    "251~" -> cb.greaterThan(root.get("itemPage"), 250)
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
}
