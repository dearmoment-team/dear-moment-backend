package kr.kro.dearmoment.common.exception

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus

class GlobalExceptionHandlerTest : DescribeSpec({

    val exceptionHandler = GlobalExceptionHandler()

    describe("GlobalExceptionHandler") {
        it("ConstraintViolationException 발생 시 BAD_REQUEST 반환") {
            val exception = ConstraintViolationException("Validation error", emptySet())
            val response = exceptionHandler.handleValidationException(exception)

            response.statusCode shouldBe HttpStatus.BAD_REQUEST
            response.body?.message shouldBe "Validation error"
        }

        it("CustomException 발생 시 정의된 상태 코드 반환") {
            val exception = CustomException(ErrorCode.INQUIRY_NOT_FOUND)
            val response = exceptionHandler.handleCustomException(exception)

            response.statusCode shouldBe HttpStatus.NOT_FOUND
            response.body?.message shouldBe exception.errorCode.message
        }

        it("IllegalArgumentException 발생 시 BAD_REQUEST 반환") {
            val exception = IllegalArgumentException("Invalid argument")
            val response = exceptionHandler.handleIllegalArgumentException(exception)

            response.statusCode shouldBe HttpStatus.BAD_REQUEST
            response.body?.message shouldBe "Invalid argument"
        }

        it("IllegalStateException 발생 시 BAD_REQUEST 반환") {
            val exception = IllegalStateException("Illegal state occurred")
            val response = exceptionHandler.handleIllegalStateException(exception)

            response.statusCode shouldBe HttpStatus.INTERNAL_SERVER_ERROR
            response.body?.message shouldBe "Illegal state occurred"
        }

        it("Exception 발생 시 INTERNAL_SERVER_ERROR 반환") {
            val exception = Exception("Unexpected error")
            val response = exceptionHandler.handleGlobalException(exception)

            response.statusCode shouldBe HttpStatus.INTERNAL_SERVER_ERROR
            response.body?.message shouldBe "Unexpected error"
        }
    }
})
