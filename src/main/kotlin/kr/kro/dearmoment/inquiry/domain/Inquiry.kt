package kr.kro.dearmoment.inquiry.domain

sealed class Inquiry(
    val id: Long = 0L,
    val userId: Long,
) {
    init {
        require(userId > 0) { "유저 ID는 양수여야 합니다." }
    }
}
