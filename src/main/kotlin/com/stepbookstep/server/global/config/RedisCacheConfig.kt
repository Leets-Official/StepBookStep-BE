package com.stepbookstep.server.global.config

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.SerializationException
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

@Configuration
@EnableCaching
class RedisCacheConfig {

    @Bean
    fun cacheManager(redisConnectionFactory: RedisConnectionFactory): CacheManager {
        val objectMapper = ObjectMapper()
        objectMapper.registerModule(JavaTimeModule())
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        objectMapper.activateDefaultTyping(
            BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Any::class.java)
                .build(),
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
        )

        val serializer = object : RedisSerializer<Any> {
            override fun serialize(t: Any?): ByteArray {
                if (t == null) {
                    return ByteArray(0)
                }
                try {
                    return objectMapper.writeValueAsBytes(t)
                } catch (e: Exception) {
                    throw SerializationException("Could not write JSON: " + e.message, e)
                }
            }

            override fun deserialize(bytes: ByteArray?): Any? {
                if (bytes == null || bytes.isEmpty()) {
                    return null
                }
                try {
                    return objectMapper.readValue(bytes, Any::class.java)
                } catch (e: Exception) {
                    throw SerializationException("Could not read JSON: " + e.message, e)
                }
            }
        }

        val defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1))
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer())
            )
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(serializer)
            )

        val cacheConfigurations = mapOf(
            "genreBooks" to defaultConfig.entryTtl(Duration.ofHours(6)),
            "under200Books" to defaultConfig.entryTtl(Duration.ofHours(12)),
            "bestsellerBooks" to defaultConfig.entryTtl(Duration.ofHours(12)),
            "bookDetail" to defaultConfig.entryTtl(Duration.ofHours(24)),
            "booksByLevel" to defaultConfig.entryTtl(Duration.ofHours(6))
        )

        return RedisCacheManager.builder(redisConnectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build()
    }
}
