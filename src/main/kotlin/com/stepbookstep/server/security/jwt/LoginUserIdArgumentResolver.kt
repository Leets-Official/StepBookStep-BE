package com.stepbookstep.server.security.jwt

import com.stepbookstep.server.global.response.CustomException
import com.stepbookstep.server.global.response.ErrorCode
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

/**
 * @LoginUserId 어노테이션이 붙은 파라미터에
 * request attribute의 userId를 주입하는 ArgumentResolver
 */
@Component
class LoginUserIdArgumentResolver : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(LoginUserId::class.java)
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Long? {
        val userId = webRequest.getAttribute("userId", RequestAttributes.SCOPE_REQUEST) as? Long

        // userId가 null이면 예외 처리
        if (userId == null && !parameter.isOptional) {
            throw CustomException(ErrorCode.INVALID_LOGIN)
        }

        return userId
    }
}
