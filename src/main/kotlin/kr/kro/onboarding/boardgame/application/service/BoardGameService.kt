package kr.kro.onboarding.boardgame.application.service

import kr.kro.onboarding.boardgame.application.port.input.GetBoardGameQuery
import org.springframework.stereotype.Service

@Service
class BoardGameService : GetBoardGameQuery {
    override fun getBoardGame() {
        TODO("Not yet implemented")
    }
}
