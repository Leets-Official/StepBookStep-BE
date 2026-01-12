package com.stepbookstep.server.security.jwt

import com.stepbookstep.server.global.response.CustomException
import com.stepbookstep.server.global.response.ErrorCode
import io.jsonwebtoken.*
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.security.Key
import java.util.Date

/**
 * HS256 기반 Access/Refresh 토큰 발급
 * - 토큰 검증 및 userId 추출
 */
@Component
class JwtProvider(
    private val props: JwtProperties
) {
    private val signingKey: Key = run {
        val keyBytes = Decoders.BASE64.decode(props.key)
        Keys.hmacShaKeyFor(keyBytes)
    }

    fun createToken(userId: Long, tokenType: TokenType): String {
        val now = Date()
        val expMillis = when (tokenType) {
            TokenType.ACCESS -> props.access.expiration
            TokenType.REFRESH -> props.refresh.expiration
        }
        val expiry = Date(now.time + expMillis)

        return Jwts.builder()
            .setSubject(userId.toString())
            .claim("typ", tokenType.name)
            .setIssuedAt(now)
            .setExpiration(expiry)
            .signWith(signingKey, SignatureAlgorithm.HS256)
            .compact()
    }

    /**
     * 토큰에서 userId 추출 (검증 포함)
     */
    fun getUserId(token: String): Long {
        val claims = parseClaims(token)
        return claims.subject.toLongOrNull()
            ?: throw CustomException(ErrorCode.TOKEN_INVALID)
    }

    /**
     * typ 클레임(TokenType) 확인
     */
    fun getTokenType(token: String): TokenType {
        val claims = parseClaims(token)
        val type = claims["typ"]?.toString() ?: throw CustomException(ErrorCode.TOKEN_INVALID)
        return runCatching { TokenType.valueOf(type) }
            .getOrElse { throw CustomException(ErrorCode.TOKEN_UNSUPPORTED) }
    }

    /**
     * 토큰 만료 여부(만료면 true)
     */
    fun isExpired(token: String): Boolean {
        return try {
            val claims = parseClaims(token)
            claims.expiration.before(Date())
        } catch (e: ExpiredJwtException) {
            true
        }
    }

    private fun parseClaims(token: String): Claims {
        try {
            return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .body
        } catch (e: ExpiredJwtException) {
            throw CustomException(ErrorCode.TOKEN_EXPIRED)
        } catch (e: UnsupportedJwtException) {
            throw CustomException(ErrorCode.TOKEN_UNSUPPORTED)
        } catch (e: MalformedJwtException) {
            throw CustomException(ErrorCode.TOKEN_INVALID)
        } catch (e: SecurityException) {
            throw CustomException(ErrorCode.TOKEN_INVALID)
        } catch (e: IllegalArgumentException) {
            throw CustomException(ErrorCode.TOKEN_NOT_FOUND)
        }
    }
}

