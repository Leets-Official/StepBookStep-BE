package com.stepbookstep.server.global.response

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.validation.FieldError

/**
 * 파라미터 오류에 대한 예외처리 DTO입니다.
 * 유효성 검증 실패 시 어떤 필드에서 에러가 발생했는지 상세 정보를 제공합니다.
 */
@Schema(
    name = "[응답][공통] 필드 에러 Response",
    description = "파라미터 검증 시 발생한 필드별 오류 정보를 담는 DTO입니다."
)
data class FieldErrorResponse(
    @Schema(description = "에러가 발생한 필드명", example = "username")
    val field: String,

    @Schema(description = "해당 필드의 에러 메시지", example = "필수 입력 값입니다.")
    val message: String?
) {
    companion object {
        /**
         * Spring의 FieldError 객체로부터 FieldErrorResponse를 생성합니다.
         *
         * @param fieldError Spring Validation의 FieldError 객체
         * @return FieldErrorResponse 인스턴스
         */
        fun from(fieldError: FieldError): FieldErrorResponse {
            return FieldErrorResponse(
                field = fieldError.field,
                message = fieldError.defaultMessage
            )
        }
    }
}