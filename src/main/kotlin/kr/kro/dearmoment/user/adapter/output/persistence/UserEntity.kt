package kr.kro.dearmoment.user.adapter.output.persistence

import jakarta.persistence.*
import kr.kro.dearmoment.common.persistence.Auditable
import kr.kro.dearmoment.user.domain.User
import java.time.LocalDateTime
import java.util.UUID
import org.hibernate.annotations.UuidGenerator
import org.hibernate.type.SqlTypes
import org.hibernate.annotations.JdbcTypeCode

@Entity
@Table(name = "users")
class UserEntity(
    @Id
    @UuidGenerator
    @GeneratedValue
    @Column(name = "user_id", columnDefinition = "RAW(16)")
    @JdbcTypeCode(SqlTypes.VARBINARY)
    var id: UUID? = null,

    @Column(nullable = true)
    var loginId: String? = null,

    @Column(nullable = true)
    var password: String? = null,

    @Column(nullable = false)
    var name: String,

    var isStudio: Boolean? = false,

    // 카카오 OAuth 식별자 (중복 가입 방지를 위해 unique 권장)
    @Column(nullable = true, unique = true)
    var kakaoId: Long? = null,

    @Column(nullable = false)
    var createdAt: LocalDateTime,

    @Column
    var updatedAt: LocalDateTime? = null
) : Auditable() {

    fun toDomain(): User {
        return User(
            id = this.id,
            loginId = this.loginId,
            password = this.password,
            name = this.name,
            isStudio = this.isStudio,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            kakaoId = this.kakaoId
        )
    }

    companion object {
        fun from(domain: User): UserEntity {
            return UserEntity(
                id = domain.id,
                loginId = domain.loginId,
                password = domain.password,
                name = domain.name,
                isStudio = domain.isStudio,
                kakaoId = domain.kakaoId,
                createdAt = domain.createdAt,
                updatedAt = domain.updatedAt
            )
        }
    }

    fun update(entity: UserEntity) {
        this.loginId = entity.loginId
        this.password = entity.password
        this.name = entity.name
        this.isStudio = entity.isStudio
        this.kakaoId = entity.kakaoId
        this.createdAt = entity.createdAt
        this.updatedAt = entity.updatedAt
    }
}
