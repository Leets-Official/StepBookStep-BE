package com.stepbookstep.server.security.jwt

/**
 * 컨트롤러 메서드 파라미터에서 로그인한 사용자의 userId를 주입받기 위한 커스텀 어노테이션
 * - request attribute의 "userId" 값을 주입
 * - Spring Security의 @AuthenticationPrincipal과 충돌 방지
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class LoginUserId
