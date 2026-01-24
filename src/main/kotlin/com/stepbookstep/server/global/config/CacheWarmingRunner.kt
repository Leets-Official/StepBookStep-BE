package com.stepbookstep.server.global.config

import com.stepbookstep.server.domain.book.domain.BookGenre
import com.stepbookstep.server.domain.home.application.HomeCacheService
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class CacheWarmingRunner(
    private val homeCacheService: HomeCacheService
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(javaClass)

    @Async
    override fun run(args: ApplicationArguments) {
        log.info("Cache warming started...")

        try {
            BookGenre.entries.forEach { genre ->
                homeCacheService.getGenreBooks(genre.displayName)
                log.info("Cached genre: ${genre.displayName}")
            }

            homeCacheService.getUnder200Books()
            log.info("Cached under200Books")

            homeCacheService.getBestsellerBooks()
            log.info("Cached bestsellerBooks")

            log.info("Cache warming completed successfully")
        } catch (e: Exception) {
            log.error("Cache warming failed: ${e.message}", e)
        }
    }
}
