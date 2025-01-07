package kr.kro.onboarding.boardgame.adapter.input.web

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class BoardGameController {
    @GetMapping("/health")
    fun healthCheck(): String {
        return "OK"
    }
}
