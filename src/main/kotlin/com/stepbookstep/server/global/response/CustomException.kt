package com.stepbookstep.server.global.response

/**
 * 비즈니스 로직에서 발생하는 커스텀 예외 클래스입니다.
 * ErrorCode를 통해 어떤 에러인지 명확하게 전달합니다.
 */
class CustomException(
    val errorCode: ErrorCode,
    val fieldErrorResponses: List<FieldErrorResponse>? = null
) : RuntimeException(errorCode.message) {

    // ErrorCode만으로 예외를 생성하는 생성자
    // 주로 단순 비즈니스 로직 예외에 사용
    constructor(errorCode: ErrorCode) : this(errorCode, null)

    // 로그나 디버깅을 위한 추가 정보 출력
    override fun toString(): String {
        return "CustomException(errorCode=${errorCode.name}, code=${errorCode.code}, message='${errorCode.message}', fieldErrors=${fieldErrorResponses?.size ?: 0})"
    }
}