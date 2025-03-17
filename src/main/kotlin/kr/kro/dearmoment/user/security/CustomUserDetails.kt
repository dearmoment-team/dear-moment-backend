package kr.kro.dearmoment.user.security

import kr.kro.dearmoment.user.domain.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.UUID

class CustomUserDetails(private val user: User) : UserDetails {

    val id: UUID
        get() = user.id as UUID

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return if (user.isStudio == true) {
            mutableListOf(SimpleGrantedAuthority("ROLE_AUTHOR"))
        } else {
            mutableListOf(SimpleGrantedAuthority("ROLE_USER"))
        }
    }

    override fun getPassword(): String = user.password

    // 로그인 시에는 기존 loginId를 사용 (사용자 이름으로도 활용 가능)
    override fun getUsername(): String = user.loginId

    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = true

    fun getUser(): User = user
}
