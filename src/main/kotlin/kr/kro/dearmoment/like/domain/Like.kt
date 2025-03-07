package kr.kro.dearmoment.like.domain

sealed class Like(
    val id: Long = 0L,
    val userId: Long,
) {
    init {
        require(userId > 0) { "유저 ID는 양수이어야 합니다." }
    }
}
