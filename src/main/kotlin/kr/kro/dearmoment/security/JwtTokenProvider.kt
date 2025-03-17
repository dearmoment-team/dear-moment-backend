package kr.kro.dearmoment.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import kr.kro.dearmoment.user.security.CustomUserDetails
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date
import java.util.UUID

@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}") private val jwtSecret: String,
    @Value("\${jwt.expiration}") private val jwtExpirationInMs: Long
) {

    fun generateToken(customUserDetails: CustomUserDetails): String {
        val now = Date()
        val expiryDate = Date(now.time + jwtExpirationInMs)
        // subject에 사용자 고유 식별자(UUID)를 저장합니다.
        return Jwts.builder()
            .setSubject(customUserDetails.id.toString())
            .claim("roles", customUserDetails.authorities.map { it.authority })
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(SignatureAlgorithm.HS512, jwtSecret)
            .compact()
    }

    fun validateToken(token: String): Boolean {
        return try {
            val claims = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token)
            !claims.body.expiration.before(Date())
        } catch (ex: Exception) {
            false
        }
    }

    fun getUserIdFromToken(token: String): UUID {
        val subject = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).body.subject
        return UUID.fromString(subject)
    }
}
