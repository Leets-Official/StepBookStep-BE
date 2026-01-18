package com.stepbookstep.server.domain.book.domain

enum class BookOrigin(val displayName: String) {
    KOREAN_NOVEL("한국소설"),
    JAPANESE_NOVEL("일본소설"),
    CHINESE_NOVEL("중국소설"),
    ENGLISH_NOVEL("영미소설"),
    FRENCH_NOVEL("프랑스소설"),
    GERMAN_NOVEL("독일소설")
}

enum class BookGenre(val displayName: String) {
    MYSTERY("추리/미스터리"),
    LIGHT_NOVEL("라이트 노벨"),
    FANTASY("판타지/환상문학"),
    HISTORICAL("역사소설"),
    SF("과학소설(SF)"),
    HORROR("호러/공포소설"),
    MARTIAL_ARTS("무협소설"),
    ACTION_THRILLER("액션/스릴러"),
    ROMANCE("로맨스"),
    POETRY("시"),
    DRAMA("희곡")
}
