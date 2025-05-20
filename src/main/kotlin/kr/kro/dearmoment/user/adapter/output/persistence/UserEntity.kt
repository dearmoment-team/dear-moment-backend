package kr.kro.dearmoment.user.adapter.output.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import kr.kro.dearmoment.common.persistence.Auditable
import kr.kro.dearmoment.user.domain.Sex
import kr.kro.dearmoment.user.domain.User
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.UuidGenerator
import org.hibernate.type.SqlTypes
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

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
    @Column(nullable = true)
    var birthDate: LocalDate? = null,
    @Enumerated(EnumType.STRING)
    @Column(nullable = true, length = 10)
    var sex: Sex? = null,
    @Column(
        name = "add_info_is_skip",
        nullable = true,
        columnDefinition = "NUMBER(1)"
    )
    var addInfoIsSkip: Boolean? = false,
    @Column(nullable = false)
    var createdAt: LocalDateTime,
    @Column
    var updatedAt: LocalDateTime? = null,
) : Auditable() {
    fun isStudioUser(): Boolean {
        return isStudio == true
    }

    fun toDomain(): User =
        User(
            id = id,
            loginId = loginId,
            password = password,
            name = name,
            isStudio = isStudio,
            kakaoId = kakaoId,
            birthDate = birthDate,
            sex = sex,
            addInfoIsSkip = addInfoIsSkip,
            createdAt = createdAt,
            updatedAt = updatedAt
        )

    companion object {
        fun from(domain: User): UserEntity =
            UserEntity(
                id = domain.id,
                loginId = domain.loginId,
                password = domain.password,
                name = domain.name,
                isStudio = domain.isStudio,
                kakaoId = domain.kakaoId,
                birthDate = domain.birthDate,
                sex = domain.sex,
                addInfoIsSkip = domain.addInfoIsSkip,
                createdAt = domain.createdAt,
                updatedAt = domain.updatedAt
            )
    }

    fun update(entity: UserEntity) {
        this.loginId = entity.loginId
        this.password = entity.password
        this.name = entity.name
        this.isStudio = entity.isStudio
        this.kakaoId = entity.kakaoId
        this.birthDate = entity.birthDate
        this.sex = entity.sex
        this.addInfoIsSkip = entity.addInfoIsSkip
        this.createdAt = entity.createdAt
        this.updatedAt = entity.updatedAt
    }
}
