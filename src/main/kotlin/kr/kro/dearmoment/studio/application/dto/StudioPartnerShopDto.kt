package kr.kro.dearmoment.studio.application.dto

import jakarta.validation.constraints.NotBlank
import kr.kro.dearmoment.studio.application.command.StudioPartnerShopCommand

data class StudioPartnerShopDto(
    @field:NotBlank(message = "제휴 업체 구분은 필수입니다.")
    val category: String,
    @field:NotBlank(message = "제휴 업체 이름은 필수입니다.")
    val name: String,
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
