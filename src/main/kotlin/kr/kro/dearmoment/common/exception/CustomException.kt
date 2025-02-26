package kr.kro.dearmoment.common.exception

class CustomException(val errorCode: ErrorCode) : RuntimeException() {
    override fun fillInStackTrace(): Throwable {
        return if (java.lang.Boolean.getBoolean("debugMode")) {
            super.fillInStackTrace()
        } else {
            this // 스택 트레이스 생성을 막음
        }
    }
}
