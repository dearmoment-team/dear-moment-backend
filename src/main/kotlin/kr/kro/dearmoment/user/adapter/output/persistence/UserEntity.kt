package kr.kro.dearmoment.user.adapter.output.persistence

import jakarta.persistence.*
import kr.kro.dearmoment.common.persistence.Auditable
import kr.kro.dearmoment.user.domain.User
import java.time.LocalDateTime

@Entity
@Table(name = "users")
class UserEntity(
    loginId: String,
    password: String,
    name: String,
    isStudio: Boolean,
    createdAt: LocalDateTime,
    updatedAt: LocalDateTime,
    createdUser: String,
    updatedUser: String,
) : Auditable() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    val id: Long = 0L

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
    var isStudio: Boolean = isStudio
        protected set

    @Column(nullable = false)
    var createdAt: LocalDateTime = createdAt
        protected set

    @Column(nullable = false)
    var updatedAt: LocalDateTime = updatedAt
        protected set

    /**
     * UserEntity 업데이트 메서드
     * 필요한 경우에만 사용 (ex: 서비스에서 수정 시)
     */
    fun update(entity: UserEntity) {
        this.loginId = entity.loginId
        this.password = entity.password
        this.name = entity.name
        this.isStudio = entity.isStudio
        this.createdAt = entity.createdAt
        this.updatedAt = entity.updatedAt
    }

    /**
     * 엔티티 -> 도메인 변환
     */
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
        /**
         * 도메인 -> 엔티티 변환
         */
        fun from(domain: User): UserEntity {
            return UserEntity(
                loginId = domain.loginId,
                password = domain.password,
                name = domain.name,
                isStudio = domain.isStudio,
                createdAt = domain.createdAt,
                updatedAt = domain.updatedAt,
                createdUser = domain.createUser,
                updatedUser = domain.updatedUser
            )
        }
    }
}
