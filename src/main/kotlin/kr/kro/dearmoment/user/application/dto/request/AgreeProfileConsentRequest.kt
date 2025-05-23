package kr.kro.dearmoment.user.application.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Past
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import kr.kro.dearmoment.user.application.command.AgreeProfileConsentCommand
import kr.kro.dearmoment.user.domain.Sex
import java.time.LocalDate

data class AgreeProfileConsentRequest(
    @field:Size(min = 2, max = 8, message = "이름은 2~8자 이내여야 합니다.")
    @field:Pattern(regexp = "^[가-힣0-9]+$", message = "이름은 한글 또는 숫자만 가능합니다.")
    @field:NotNull
    @Schema(description = "새로운 이름 (한글/숫자 2~8자)", example = "예랑이123")
    val name: String,
    @field:Past(message = "생년월일은 과거 날짜여야 합니다.")
    @Schema(description = "생년월일 (YYYY-MM-DD)", example = "1995-05-13")
    @field:NotNull
    val birthDate: LocalDate,
    @field:NotNull
    @Schema(
        description = "성별",
        allowableValues = ["MALE", "FEMALE"],
        example = "MALE"
    )
    val sex: Sex,
    @field:NotNull
    @Schema(
        description = "추가 입력 정보 동의 여부",
        example = "true"
    )
    val addInfoIsAgree: Boolean,
) {
    fun toCommand() =
        AgreeProfileConsentCommand(
            name = name,
            birthDate = birthDate,
            sex = sex,
            addInfoIsAgree = addInfoIsAgree,
        )
}
