package kr.kro.dearmoment.like.application.query

data class GetLikesQuery(
    val userId: Long,
    val likeType: String,
)
