package com.stepbookstep.server.auth

/**
 * 사용자 디바이스 정보 활용시 사용되는 인터페이스 (현재 사용 X)
 */
interface UserDeviceService {
    fun upsertFcmToken(userId: Long, fcmToken: String)
}
