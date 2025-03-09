package kr.kro.dearmoment.common.exception

class CustomException(val errorCode: ErrorCode) : RuntimeException() {
    override fun fillInStackTrace(): Throwable {
        return this // 스택 트레이스 생성을 막음
    }

    override fun toString(): String = "CustomException(code=${errorCode.name}, status=${errorCode.status}, message='${errorCode.message}')"
}
