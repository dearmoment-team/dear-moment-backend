package kr.kro.dearmoment.user.adapter.output.persistence

import kr.kro.dearmoment.common.exception.CustomException
import kr.kro.dearmoment.common.exception.ErrorCode
import kr.kro.dearmoment.user.application.port.output.GetStudioUserPort
import kr.kro.dearmoment.user.application.port.output.GetUserByIdPort
import kr.kro.dearmoment.user.application.port.output.GetUserByKakaoIdPort
import kr.kro.dearmoment.user.application.port.output.SaveUserPort
import kr.kro.dearmoment.user.domain.User
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class UserPersistenceAdapter(
    private val userJpaRepository: UserJpaRepository,
) : SaveUserPort, GetUserByIdPort, GetUserByKakaoIdPort, GetStudioUserPort {
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

    override fun findByKakaoId(kakaoId: Long): User? {
        val entity = userJpaRepository.findByKakaoId(kakaoId)
        return entity?.toDomain()
    }

    override fun findStudioUserById(id: UUID): User {
        val entity =
            userJpaRepository.findById(id)
                ?: throw CustomException(ErrorCode.USER_NOT_FOUND)
        if (!entity.isStudioUser()) {
            throw CustomException(ErrorCode.UNAUTHORIZED_ACCESS)
        }
        return entity.toDomain()
    }
}
