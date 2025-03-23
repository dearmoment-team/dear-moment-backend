package kr.kro.dearmoment.common.exception

class CustomException(val errorCode: ErrorCode) : RuntimeException(errorCode.message) {
    override fun fillInStackTrace(): Throwable {
        return this // 스택 트레이스 생성을 막음
    }
}
