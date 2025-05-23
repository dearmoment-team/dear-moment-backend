package kr.kro.dearmoment.user.application.service

import kr.kro.dearmoment.studio.application.port.output.GetStudioPort
import kr.kro.dearmoment.user.application.command.AgreeProfileConsentCommand
import kr.kro.dearmoment.user.application.dto.request.UpdateUserRequest
import kr.kro.dearmoment.user.application.dto.response.UserStudioResponse
import kr.kro.dearmoment.user.application.port.input.AgreeProfileConsentUseCase
import kr.kro.dearmoment.user.application.port.input.SkipProfileConsentUseCase
import kr.kro.dearmoment.user.application.port.input.WithdrawProfileConsentUseCase
import kr.kro.dearmoment.user.application.port.output.GetUserByIdPort
import kr.kro.dearmoment.user.application.port.output.SaveUserPort
import kr.kro.dearmoment.user.domain.User
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class UserProfileService(
    private val getUserByIdPort: GetUserByIdPort,
    private val saveUserPort: SaveUserPort,
    private val getStudioPort: GetStudioPort,
) : AgreeProfileConsentUseCase,
    SkipProfileConsentUseCase,
    WithdrawProfileConsentUseCase {
    fun getProfile(userId: UUID): UserStudioResponse {
        val user =
            getUserByIdPort.findById(userId)
                ?: throw UsernameNotFoundException("User not found with id: $userId")

        val studioId = getStudioPort.findByUserId(userId)?.id ?: 0L

        return UserStudioResponse.from(user, studioId)
    }

    fun updateUser(
        userId: UUID,
        req: UpdateUserRequest
    ): User {
        val existing = load(userId)
        val now = LocalDateTime.now()
        val updated =
            existing.updateProfile(
                newName = req.name,
                newIsStudio = req.isStudio,
                newBirthDate = req.birthDate,
                newSex = req.sex,
                newAddInfoIsSkip = req.addInfoIsSkip,
                newAddInfoIsAgree = req.addInfoIsAgree,
                now = now
            )
        return saveUserPort.save(updated)
    }

    // 1) 스킵
    override fun skipAddInfo(userId: UUID): User = saveUserPort.save(load(userId).skipAddInfo(LocalDateTime.now()))

    // 2) 동의 / 거부
    override fun agreeAddInfo(
        userId: UUID,
        cmd: AgreeProfileConsentCommand
    ): User =
        saveUserPort.save(
            load(userId).agreeAddInfo(
                name = cmd.name,
                birth = cmd.birthDate,
                sex = cmd.sex,
                agreeFlag = cmd.addInfoIsAgree,
                now = LocalDateTime.now()
            )
        )

    // 3) 철회
    override fun withdrawAddInfo(userId: UUID) {
        saveUserPort.save(load(userId).withdrawAddInfo(LocalDateTime.now()))
    }

    // --- 공용 로딩 헬퍼 ---
    private fun load(id: UUID) =
        getUserByIdPort.findById(id)
            ?: throw UsernameNotFoundException("User $id not found")
}
