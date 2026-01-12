package com.stepbookstep.server.user

import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {

    fun findByProviderAndProviderUserId(
        provider: String,
        providerUserId: String
    ): User?
}
