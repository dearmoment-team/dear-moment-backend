package kr.kro.dearmoment.common.advice

import kr.kro.dearmoment.common.dto.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(ErrorResponse(e.message ?: "Error occurred"), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(e: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(ErrorResponse(e.message ?: "Internal Server Error"), HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
