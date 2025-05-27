package kr.kro.dearmoment.user.domain

enum class WithdrawalReason(val code: Int) {
    COMPLETED_WEDDING(1),
    NO_PHOTOGRAPHER_FOUND(2),
    INCONVENIENT_UI(3),
    FOUND_ELSEWHERE(4),
    NO_AVAILABLE_ARTIST(5),
    OTHER(6);

    companion object {
        fun from(code: Int) = entries.first { it.code == code }
    }
}
