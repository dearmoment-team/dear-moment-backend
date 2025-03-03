package kr.kro.dearmoment.studio.domain

enum class StudioStatus {
    ACTIVE,
    INACTIVE,
    ;

    companion object {
        fun from(value: String): StudioStatus {
            return StudioStatus.entries.find { it.name == value }
                ?: throw IllegalArgumentException("유효하지 않은 StudioStatus 값: $value")
        }
    }
}
