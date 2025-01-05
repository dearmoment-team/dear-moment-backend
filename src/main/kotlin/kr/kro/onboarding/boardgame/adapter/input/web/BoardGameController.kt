package kr.kro.onboarding.boardgame.adapter.input.web

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Tag(name = "Board Game API", description = "보드 게임 관련 API")
class BoardGameController {

    @GetMapping("/health")
    @Operation(
        summary = "헬스",
        description = "서버 상태를 확인하기 위한 API입니다.",
        tags = ["Health Check"]
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "서버가 정상적으로 동작 중입니다."),
            ApiResponse(responseCode = "500", description = "서버에 문제가 발생했습니다.")
        ]
    )
    fun healthCheck(): String {
        return "OK"
    }
}
