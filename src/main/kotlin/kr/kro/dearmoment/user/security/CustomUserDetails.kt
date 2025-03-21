package kr.kro.dearmoment.user.security

import kr.kro.dearmoment.user.domain.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.UUID

class CustomUserDetails(private val user: User) : UserDetails {

    val id: UUID
        get() = user.id ?: throw IllegalStateException("User ID가 존재하지 않습니다.")

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return if (user.isStudio == true) {
            mutableListOf(SimpleGrantedAuthority("ROLE_ARTIST"))
        } else {
            mutableListOf(SimpleGrantedAuthority("ROLE_USER"))
        }
    }

    override fun getPassword(): String = user.password ?: ""

    override fun getUsername(): String = user.loginId ?: ""

    override fun isAccountNonExpired() = true
    override fun isAccountNonLocked() = true
    override fun isCredentialsNonExpired() = true
    override fun isEnabled() = true

    fun getUser(): User = user
}
