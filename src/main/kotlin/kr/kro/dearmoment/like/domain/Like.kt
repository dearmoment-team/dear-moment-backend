package kr.kro.dearmoment.like.domain

class Like(
    val id: Long = 0L,
    val userId: Long,
    val targetId: Long,
    val type: LikeType,
) {
    init {
        require(id > 0) { "좋아요 ID는 양수이어야 합니다." }
        require(userId > 0) { "유저 ID는 양수이어야 합니다." }
        require(targetId > 0) { "좋아요 대상 ID는 양수이어야 합니다." }
    }
}
