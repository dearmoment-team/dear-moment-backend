package kr.kro.dearmoment.user.security

import kr.kro.dearmoment.user.application.port.output.GetUserByIdPort
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class CustomUserDetailsService(
    private val getUserByIdPort: GetUserByIdPort, // UUID를 기준으로 조회하는 포트
) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        // 만약 기존 loginId 기반 조회가 필요하면 이 메서드를 유지
        val user =
            getUserByIdPort.findByLoginId(username)
                ?: throw UsernameNotFoundException("User not found with loginId: $username")
        return CustomUserDetails(user)
    }

    fun loadUserById(userId: UUID): CustomUserDetails {
        val user =
            getUserByIdPort.findById(userId)
                ?: throw UsernameNotFoundException("User not found with id: $userId")
        return CustomUserDetails(user)
    }
}
