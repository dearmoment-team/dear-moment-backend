package kr.kro.dearmoment.studio.application.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import kr.kro.dearmoment.common.validation.EnumValue
import kr.kro.dearmoment.studio.application.command.StudioPartnerShopCommand
import kr.kro.dearmoment.studio.domain.StudioPartnerShopCategory

data class StudioPartnerShopDto(
    @Schema(
        description = "파트너샵 카테고리",
        example = "HAIR_MAKEUP",
        allowableValues = ["WEDDING_SHOP, HAIR_MAKEUP", "DRESS", "MENS_SUIT", "BOUQUET", "VIDEO", "STUDIO", "ETC"],
    )
    @field:NotBlank(message = "제휴 업체 구분은 필수입니다.")
    @field:EnumValue(enumClass = StudioPartnerShopCategory::class, message = "유효하지 제휴 업체입니다.")
    val category: String,
    @Schema(description = "파트너샵 이름", example = "샘플샵")
    @field:NotBlank(message = "제휴 업체 이름은 필수입니다.")
    val name: String,
    @Schema(description = "파트너샵 링크", example = "http://example.com")
    @field:NotBlank(message = "제휴 업체 링크는 필수입니다.")
    val urlLink: String,
) {
    fun toCommand() =
        StudioPartnerShopCommand(
            category = category,
            name = name,
            urlLink = urlLink,
        )
}
