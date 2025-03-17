package kr.kro.dearmoment.user.adapter.output.persistence

import kr.kro.dearmoment.user.application.port.output.GetUserByIdPort
import kr.kro.dearmoment.user.application.port.output.SaveUserPort
import kr.kro.dearmoment.user.domain.User
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class UserPersistenceAdapter(
    private val userJpaRepository: UserJpaRepository
) : SaveUserPort, GetUserByIdPort {

    override fun save(user: User): User {
        val entity = UserEntity.from(user)
        val saved = userJpaRepository.save(entity)
        return saved.toDomain()
    }

    override fun findById(id: UUID): User? {
        val entity = userJpaRepository.findById(id)
        return entity?.toDomain()
    }

    override fun findByLoginId(loginId: String): User? {
        val entity = userJpaRepository.findByLoginId(loginId)
        return entity?.toDomain()
    }
}
