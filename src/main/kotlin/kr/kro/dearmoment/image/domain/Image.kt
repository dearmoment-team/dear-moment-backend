package kr.kro.dearmoment.image.domain

import java.time.LocalDateTime

data class Image(
    val imageId: Long = 0L,
    val userId: Long,
    val parId: String = "",
    val fileName: String,
    val url: String = "",
    val urlExpireTime: LocalDateTime = LocalDateTime.now(),
) {
    fun isUrlExpired() = this.urlExpireTime <= LocalDateTime.now()
}

fun Image.withUserId(userId: Long): Image =
    Image(
        imageId = this.imageId,
        userId = userId,
        parId = this.parId,
        fileName = this.fileName,
        url = this.url,
        urlExpireTime = this.urlExpireTime,
    )
