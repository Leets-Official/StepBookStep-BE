package com.stepbookstep.server.global.response

import org.springframework.http.HttpStatus

/**
 * 애플리케이션 전역에서 사용하는 에러 코드 Enum입니다.
 * 각 에러는 고유 코드, HTTP 상태, 메시지를 포함합니다.
 *
 * 0~999 : 공통 에러
 * 1000~1999 : 유저 관련 에러
 * 2000~2999 : 책 관련 에러
 * 3000~3999 : 루틴 관련 에러
 * 4000~4999 : 검색어 관련 에러
 * 5000~5999 : 독서 기록/목표 관련 에러  <-- 추가
 * 10000 이상 : 기타 파라미터 등
 */
enum class ErrorCode(
    val code: Int,
    val httpStatus: HttpStatus,
    val message: String
) {

    // ========================
    // 100 Test Error
    // ========================
    TEST_ERROR(100_000, HttpStatus.BAD_REQUEST, "테스트 에러입니다."),


    // ========================
    // 400 Bad Request
    // ========================
    BAD_REQUEST(400_000, HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    INVALID_FILE_FORMAT(400_001, HttpStatus.BAD_REQUEST, "업로드된 파일 형식이 올바르지 않습니다."),
    INVALID_INPUT(400_002, HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    NULL_VALUE(400_003, HttpStatus.BAD_REQUEST, "Null 값이 들어왔습니다."),
    INVALID_NICKNAME(400_004, HttpStatus.BAD_REQUEST, "닉네임은 필수입니다."),


    // ========================
    // 401 Unauthorized
    // ========================
    TOKEN_EXPIRED(401_000, HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
    TOKEN_INVALID(401_001, HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    TOKEN_NOT_FOUND(401_002, HttpStatus.UNAUTHORIZED, "토큰이 존재하지 않습니다."),
    TOKEN_UNSUPPORTED(401_003, HttpStatus.UNAUTHORIZED, "지원하지 않는 토큰 형식입니다."),
    INVALID_CREDENTIALS(401_004, HttpStatus.UNAUTHORIZED, "인증 정보가 올바르지 않습니다."),
    INVALID_REFRESH_TOKEN(401_005, HttpStatus.UNAUTHORIZED, "재발급 토큰이 유효하지 않습니다."),
    INVALID_ACCESS_TOKEN(401_006, HttpStatus.UNAUTHORIZED, "접근 토큰이 유효하지 않습니다."),
    INVALID_TOKEN(401_007, HttpStatus.UNAUTHORIZED, "토큰이 생성되지 않았습니다."),
    INVALID_LOGIN(401_008, HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    REFRESH_TOKEN_MISMATCH(401_009, HttpStatus.UNAUTHORIZED, "저장된 리프레시 토큰과 일치하지 않습니다."),
    EXPIRED_REFRESH_TOKEN(401_010, HttpStatus.UNAUTHORIZED, "리프레시 토큰이 만료되었습니다."),
    REFRESH_TOKEN_NOT_FOUND(401_011, HttpStatus.UNAUTHORIZED, "저장된 리프레시 토큰이 존재하지 않습니다."),
    UNSUPPORTED_SOCIAL_LOGIN(401_012, HttpStatus.UNAUTHORIZED, "지원하지 않는 소셜 로그인 방식입니다."),


    // ========================
    // 403 Forbidden
    // ========================
    FORBIDDEN(403_000, HttpStatus.FORBIDDEN, "접속 권한이 없습니다."),
    ACCESS_DENY(403_001, HttpStatus.FORBIDDEN, "접근이 거부되었습니다."),
    INVALID_ENVIRONMENT(403_002, HttpStatus.FORBIDDEN, "해당 게시글에 접근할 권한이 없습니다."),


    // ========================
    // 404 Not Found
    // ========================
    NOT_FOUND_END_POINT(404_000, HttpStatus.NOT_FOUND, "요청한 대상이 존재하지 않습니다."),
    USER_NOT_FOUND(404_001, HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    USER_NOT_FOUND_IN_COOKIE(404_002, HttpStatus.NOT_FOUND, "쿠키에서 사용자 정보를 찾을 수 없습니다."),
    POST_NOT_FOUND(404_003, HttpStatus.NOT_FOUND, "요청한 게시글을 찾을 수 없습니다."),
    POST_TYPE_NOT_FOUND(404_004, HttpStatus.NOT_FOUND, "게시글 타입을 찾을 수 없습니다."),
    COMMENT_NOT_FOUND(404_005, HttpStatus.NOT_FOUND, "요청한 댓글을 찾을 수 없습니다."),
    PRODUCT_NOT_FOUND(404_006, HttpStatus.NOT_FOUND, "해당 상품을 찾을 수 없습니다."),
    GOAL_NOT_FOUND(404_007, HttpStatus.NOT_FOUND, "해당 목표를 찾을 수 없습니다."),

    // ========================
    // 409 Conflict
    // ========================
    DUPLICATE_EMAIL(409_001, HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),


    // ========================
    // 500 Internal Server Error
    // ========================
    INTERNAL_SERVER_ERROR(500, HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다."),


    // ========================
    // 999 Bad Parameter
    // ========================
    BAD_PARAMETER(999, HttpStatus.BAD_REQUEST, "요청 파라미터에 문제가 존재합니다."),


    // ========================
    // 1000~1999 : 유저 관련 응답 에러
    // ========================

    // 유저 없음
    NOT_USER(1000, HttpStatus.NOT_FOUND, "해당하는 유저가 존재하지 않습니다."),

    // 중복 확인
    DUPLICATED_NICKNAME(1300, HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),


    // ========================
    // 2000~2999 : 책 관련 에러
    // ========================
    BOOK_NOT_FOUND(2000, HttpStatus.NOT_FOUND, "해당하는 도서가 존재하지 않습니다."),
    INVALID_GENRE_ID(2001, HttpStatus.BAD_REQUEST, "유효하지 않은 장르 ID입니다. (0~10)"),


    // ========================
    // 3000~3999 : 루틴 관련 에러
    // ========================


    // ========================
    // 4000~4999 : 검색어 관련 에러
    // ========================
    NOT_SEARCH(4000, HttpStatus.NOT_FOUND, "해당하는 검색 정보가 존재하지 않습니다."),
    INVALID_SORT_TYPE(4001, HttpStatus.BAD_REQUEST, "잘못된 정렬 방식입니다."),


    // ========================
    // 5000~5999 : 독서 기록/목표 관련 에러
    // ========================
    READ_QUANTITY_REQUIRED(5000, HttpStatus.BAD_REQUEST, "읽은 페이지 수를 입력해주세요."),
    DURATION_REQUIRED(5001, HttpStatus.BAD_REQUEST, "독서 시간을 입력해주세요."),
    RATING_REQUIRED(5002, HttpStatus.BAD_REQUEST, "평점을 입력해주세요."),
    INVALID_RATING(5003, HttpStatus.BAD_REQUEST, "평점은 1-5 사이여야 합니다."),
    TARGET_AMOUNT_INVALID(5004, HttpStatus.BAD_REQUEST, "목표량은 1 이상이어야 합니다."),
    PAGE_CANNOT_GO_BACK(5005, HttpStatus.BAD_REQUEST, "이전 기록보다 적은 페이지를 입력할 수 없습니다.");

    companion object {
        /**
         * 에러 코드(숫자)를 바탕으로 ErrorCode를 반환합니다.
         *
         * @param code 에러 코드 번호
         * @return 일치하는 ErrorCode, 존재하지 않으면 null
         */
        fun fromCode(code: Int): ErrorCode? {
            return values().firstOrNull { it.code == code }
        }
    }
}