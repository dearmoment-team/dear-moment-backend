package kr.kro.dearmoment.image.domain

import java.time.LocalDateTime
import java.util.UUID

data class Image(
    val imageId: Long = 0L,
    val userId: UUID = UUID.randomUUID(),
    val parId: String = "",
    val fileName: String,
    val url: String = "",
    val urlExpireTime: LocalDateTime = LocalDateTime.now(),
) {
    fun isUrlExpired() = this.urlExpireTime <= LocalDateTime.now()
}

fun Image.withUserId(userId: UUID): Image =
    Image(
        imageId = this.imageId,
        userId = userId,
        parId = this.parId,
        fileName = this.fileName,
        url = this.url,
        urlExpireTime = this.urlExpireTime,
    )
