package com.stepbookstep.server.domain.user.domain

import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {

    fun findByProviderAndProviderUserId(
        provider: String,
        providerUserId: String
    ): User?
}