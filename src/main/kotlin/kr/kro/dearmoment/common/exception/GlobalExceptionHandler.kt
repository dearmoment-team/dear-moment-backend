package kr.kro.dearmoment.common.exception

import jakarta.validation.ConstraintViolationException
import kr.kro.dearmoment.common.dto.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(ConstraintViolationException::class)
    fun handleValidationException(e: ConstraintViolationException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(ErrorResponse(e.message ?: "Validation error"), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(ErrorResponse(e.message ?: "Validation error"), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(CustomException::class)
    fun handleCustomException(e: CustomException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(e.errorCode.message)
        return ResponseEntity(errorResponse, e.errorCode.status)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(ErrorResponse(e.message ?: "Invalid argument"), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(e: IllegalStateException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(ErrorResponse(e.message ?: "Internal Server Error"), HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(Exception::class)
    fun handleGlobalException(e: Exception): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .contentType(APPLICATION_JSON) // JSON 응답 명시
            .body(ErrorResponse(e.message ?: "Internal Server Error"))
    }
}
