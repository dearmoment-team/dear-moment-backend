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
    var id: UUID? = null,  // nullable

    loginId: String,
    password: String,
    name: String,
    var isStudio: Boolean? = false,
    createdAt: LocalDateTime,
    updatedAt: LocalDateTime? = null
) : Auditable() {

    @Column(nullable = false, unique = true)
    var loginId: String = loginId
        protected set

    @Column(nullable = false)
    var password: String = password
        protected set

    @Column(nullable = false)
    var name: String = name
        protected set

    @Column(nullable = false)
    var createdAt: LocalDateTime = createdAt
        protected set

    @Column
    var updatedAt: LocalDateTime? = updatedAt
        protected set

    fun toDomain(): User {
        return User(
            id = this.id,
            loginId = this.loginId,
            password = this.password,
            name = this.name,
            isStudio = this.isStudio,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
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
        this.createdAt = entity.createdAt
        this.updatedAt = entity.updatedAt
    }
}
