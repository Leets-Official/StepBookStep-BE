package com.stepbookstep.server.global.response

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.http.HttpStatus

/**
 * API 응답을 표준화하기 위한 데이터 클래스입니다.
 */
data class ApiResponse<T>(
    @JsonIgnore
    val httpStatus: HttpStatus,
    val success: Boolean,
    val code: Int,
    val message: String,
    val data: T? = null,
    val error: List<FieldErrorResponse>? = null
) {
    companion object {
        // 조회 성공 (200 OK)
        fun <T> ok(data: T? = null): ApiResponse<T> {
            return ApiResponse(
                httpStatus = HttpStatus.OK,
                success = true,
                code = HttpStatus.OK.value(),
                message = "호출이 성공적으로 완료되었습니다.",
                data = data,
                error = null
            )
        }

        // 조회 성공 (200 OK)
        fun <T> created(): ApiResponse<T> {
            return ApiResponse(
                httpStatus = HttpStatus.CREATED,
                success = true,
                code = HttpStatus.CREATED.value(),
                message = "성공적으로 생성되었습니다.",
                data = null,
                error = null
            )
        }

        // 데이터와 함께 생성 성공 응답 (201 Created)
        fun <T> created(data: T): ApiResponse<T> {
            return ApiResponse(
                httpStatus = HttpStatus.CREATED,
                success = true,
                code = HttpStatus.CREATED.value(),
                message = "성공적으로 생성되었습니다.",
                data = data,
                error = null
            )
        }

        // 수정 성공 응답 (204 No Content)
        fun <T> updated(): ApiResponse<T> {
            return ApiResponse(
                httpStatus = HttpStatus.OK,
                success = true,
                code = HttpStatus.OK.value(),
                message = "성공적으로 수정되었습니다.",
                data = null,
                error = null
            )
        }

        // 삭제 성공 응답 (204 No Content)
        fun <T> deleted(): ApiResponse<T> {
            return ApiResponse(
                httpStatus = HttpStatus.OK,
                success = true,
                code = HttpStatus.OK.value(),
                message = "성공적으로 삭제 되었습니다.",
                data = null,
                error = null
            )
        }

        // 실패 응답 생성
        fun <T> fail(e: CustomException): ApiResponse<T> {
            return ApiResponse(
                httpStatus = e.errorCode.httpStatus,
                success = false,
                code = e.errorCode.code,
                message = e.errorCode.message,
                data = null,
                error = e.fieldErrorResponses
            )
        }
    }
}