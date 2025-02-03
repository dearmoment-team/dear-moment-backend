package kr.kro.dearmoment.image.adapter.input.web

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthCheckController {
    @GetMapping("/health")
    fun healthCheck(): HealthCheckResponse {
        return HealthCheckResponse(value = "OK")
    }
}

data class HealthCheckResponse(
    val value: String,
)
