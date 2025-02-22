package kr.kro.dearmoment.inquiry.domain

enum class ServiceInquiryType {
    SERVICE_COMPLIMENT,
    SERVICE_SUGGESTION,
    SYSTEM_IMPROVEMENT,
    SYSTEM_ERROR_REPORT,
    ;

    companion object {
        fun from(value: String): ServiceInquiryType {
            return entries.find { it.name == value }
                ?: throw IllegalArgumentException("유효하지 않은 ServiceInquiryType 값: $value")
        }
    }
}
