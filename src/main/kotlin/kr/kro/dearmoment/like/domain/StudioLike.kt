package kr.kro.dearmoment.like.domain

import kr.kro.dearmoment.studio.domain.Studio

class StudioLike(
    id: Long = 0L,
    userId: Long,
    val studio: Studio,
) : Like(id, userId) {
    init {
        require(studio.id > 0) { "스튜디오 ID는 양수이어야 합니다." }
    }
}
