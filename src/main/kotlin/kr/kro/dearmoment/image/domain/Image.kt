package kr.kro.dearmoment.image.domain

data class Image(
    val imageId: Long = 0L,
    val userId: Long,
    val fileName: String,
    val url: String,
)
