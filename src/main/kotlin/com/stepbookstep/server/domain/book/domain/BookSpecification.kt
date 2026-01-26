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
     * 분량 필터 (~200, 201~250, 251~350, 351~500, 501~650, 651~)
     * 중복 선택 가능 (OR 조건)
     */
    fun withPageRange(pageRanges: List<String>?): Specification<Book> {
        return Specification { root, _, cb ->
            if (pageRanges.isNullOrEmpty()) {
                null
            } else {
                val predicates = pageRanges.mapNotNull { pageRange ->
                    when (pageRange) {
                        "~200" -> cb.lessThanOrEqualTo(root.get<Int>("itemPage"), 200)
                        "201~250" -> cb.and(
                            cb.greaterThan(root.get<Int>("itemPage"), 200),
                            cb.lessThanOrEqualTo(root.get<Int>("itemPage"), 250)
                        )
                        "251~350" -> cb.and(
                            cb.greaterThan(root.get<Int>("itemPage"), 250),
                            cb.lessThanOrEqualTo(root.get<Int>("itemPage"), 350)
                        )
                        "351~500" -> cb.and(
                            cb.greaterThan(root.get<Int>("itemPage"), 350),
                            cb.lessThanOrEqualTo(root.get<Int>("itemPage"), 500)
                        )
                        "501~650" -> cb.and(
                            cb.greaterThan(root.get<Int>("itemPage"), 500),
                            cb.lessThanOrEqualTo(root.get<Int>("itemPage"), 650)
                        )
                        "651~" -> cb.greaterThan(root.get<Int>("itemPage"), 650)
                        else -> null
                    }
                }
                if (predicates.isEmpty()) null else cb.or(*predicates.toTypedArray())
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
