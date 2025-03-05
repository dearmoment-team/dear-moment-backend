package kr.kro.dearmoment.inquiry.domain

enum class ServiceInquiryType(
    val desc: String,
) {
    SERVICE_COMPLIMENT("디어모먼트 서비스 칭찬"),
    SERVICE_SUGGESTION("디어모먼트 서비스 불편/제안"),
    SYSTEM_IMPROVEMENT("시스템 개선 의견"),
    SYSTEM_ERROR_REPORT("시스템 오류 제보"),
    ;

    companion object {
        fun from(value: String): ServiceInquiryType {
            return entries.find { it.name == value }
                ?: throw IllegalArgumentException("유효하지 않은 ServiceInquiryType 값: $value")
        }
    }
}
