package kr.kro.dearmoment.image.domain

import java.time.LocalDateTime

class Image(
    val imageId: Long = 0L,
    val userId: Long,
    val parId: String = "",
    val fileName: String,
    val url: String = "",
    val urlExpireTime: LocalDateTime = LocalDateTime.now(),
) {
    fun isUrlExpired() = this.urlExpireTime <= LocalDateTime.now()
}
