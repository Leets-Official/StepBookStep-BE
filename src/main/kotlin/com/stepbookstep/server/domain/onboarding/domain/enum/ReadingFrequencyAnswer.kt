package com.stepbookstep.server.domain.onboarding.domain.enum

/**
 * 온보딩 레벨 측정을 위한 독서 선호/빈도 측정 질문 enum
 */
enum class ReadingFrequencyAnswer {
    FINISHED_RECENTLY, // 최근에도 책 한 권은 끝까지 읽었어요 -> 하루
    STOP_MIDWAY, // 읽고 싶긴 한데, 자주 중간에 멈춰요 -> 하루
    LONG_TIME_NO_BOOK, // 책을 펼치는 것 자체가 오랜만이에요 -> 일주일
    DONT_KNOW_START // 솔직히 어디서부터 시작해야 할지 모르겠어요 -> 일주일
}