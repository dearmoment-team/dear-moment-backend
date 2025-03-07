package kr.kro.dearmoment.like.domain

class CreateStudioLike(
    id: Long = 0L,
    userId: Long,
    val studioId: Long,
) : Like(id, userId) {
    init {
        require(studioId > 0) { "스튜디오 ID는 양수이어야 합니다." }
    }
}
