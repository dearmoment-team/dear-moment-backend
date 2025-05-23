package kr.kro.dearmoment.user.application.command

import kr.kro.dearmoment.user.domain.Sex
import java.time.LocalDate

data class AgreeProfileConsentCommand(
    val name: String,
    val birthDate: LocalDate,
    val sex: Sex,
    val addInfoIsAgree: Boolean
)
