package kr.kro.dearmoment.like.domain

enum class LikeType(
    val value: String,
) {
    PRODUCT("PRODUCT"),
    ARTIST("ARTIST"),
    ;

    companion object {
        fun from(value: String): LikeType {
            return entries.find { it.value == value }
                ?: throw IllegalArgumentException("유효하지 않은 LikeType 값: $value")
        }
    }
}
