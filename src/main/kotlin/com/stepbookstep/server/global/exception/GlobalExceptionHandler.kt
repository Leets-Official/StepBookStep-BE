package com.stepbookstep.server.global.exception

import com.stepbookstep.server.global.response.ApiResponse
import com.stepbookstep.server.global.response.CustomException
import com.stepbookstep.server.global.response.ErrorCode
import com.stepbookstep.server.global.response.FieldErrorResponse
import io.swagger.v3.oas.annotations.Hidden
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.resource.NoResourceFoundException

/**
 * 전역 예외 처리 핸들러
 * - @Hidden 은 스웨거 문서 생성 대상에서 제외시키기 위함입니다.
 */
@Hidden
@RestControllerAdvice
class GlobalExceptionHandler {

    companion object {
        private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    }

    // 공통 처리 메서드
    private fun handleCustomException(
        customException: CustomException,
        request: HttpServletRequest
    ): ApiResponse<CustomException> {

        val username = request.userPrincipal?.name ?: "anonymous"
        val errorCode = customException.errorCode

        log.info(
            "[EXCEPTION] 사용자: {}, 메서드: {}, URI: {}, 예외: {}",
            username,
            request.method,
            request.requestURI,
            errorCode.message
        )

        return ApiResponse.fail(customException)
    }

    // CustomException 핸들러
    @ExceptionHandler(CustomException::class)
    fun handleCustomExceptionWithStatus(
        e: CustomException,
        request: HttpServletRequest
    ): ResponseEntity<ApiResponse<CustomException>> {
        val response = handleCustomException(e, request)
        return ResponseEntity
            .status(e.errorCode.httpStatus)
            .body(response)
    }

    // 500 error
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception::class)
    fun handleException(
        e: Exception,
        request: HttpServletRequest
    ): ApiResponse<CustomException> {
        log.error(e.message, e)

        val errorCode = ErrorCode.INTERNAL_SERVER_ERROR
        val exception = CustomException(errorCode, null)

        return handleCustomException(exception, request)
    }

    // 400 error
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(
        IllegalStateException::class,
        IllegalArgumentException::class
    )
    fun handleIllegalStateException(
        e: Exception,
        request: HttpServletRequest
    ): ApiResponse<CustomException> {

        val errorCode = ErrorCode.BAD_REQUEST
        val exception = CustomException(errorCode, null)

        return handleCustomException(exception, request)
    }

    // 404 error
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(
        NoSuchElementException::class,
        NoResourceFoundException::class
    )
    fun handleNoSuchException(
        e: Exception,
        request: HttpServletRequest
    ): ApiResponse<CustomException> {

        val errorCode = ErrorCode.NOT_FOUND_END_POINT
        val exception = CustomException(errorCode, null)

        return handleCustomException(exception, request)
    }

    // 400 error(validation)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(
        e: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): ApiResponse<CustomException> {

        val errorCode = ErrorCode.BAD_PARAMETER

        // BindingResult 바탕으로 필드에러 List 생성
        val errors = e.bindingResult.fieldErrors.map { fielderror ->
            FieldErrorResponse.from(fielderror)
        }

        val exception = CustomException(errorCode, errors)

        return handleCustomException(exception, request)
    }
}