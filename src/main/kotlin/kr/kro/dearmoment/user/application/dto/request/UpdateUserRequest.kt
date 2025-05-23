package kr.kro.dearmoment.user.application.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Past
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import kr.kro.dearmoment.user.domain.Sex
import java.time.LocalDate

@Schema(description = "사용자 프로필 수정 요청 DTO")
data class UpdateUserRequest(
    @field:Size(min = 2, max = 8, message = "이름은 2~8자 이내여야 합니다.")
    @field:Pattern(regexp = "^[가-힣0-9]+$", message = "이름은 한글 또는 숫자만 가능합니다.")
    @Schema(description = "새로운 이름 (한글/숫자 2~8자)", example = "예랑이123")
    val name: String? = null,
    @Schema(description = "스튜디오 유저 여부", example = "true")
    val isStudio: Boolean? = null,
    @field:Past(message = "생년월일은 과거 날짜여야 합니다.")
    @Schema(description = "생년월일 (YYYY-MM-DD)", example = "1995-05-13")
    val birthDate: LocalDate? = null,
    @Schema(
        description = "성별",
        allowableValues = ["MALE", "FEMALE"],
        example = "MALE"
    )
    val sex: Sex? = null,
    @Schema(
        description = "초기 입력 정보 스킵 유무(입력 완료시에도 true)",
        example = "false"
    )
    val addInfoIsSkip: Boolean? = null,
    @Schema(
        description = "추가 입력 정보 동의 여부",
        example = "false"
    )
    val addInfoIsAgree: Boolean? = null,
)
