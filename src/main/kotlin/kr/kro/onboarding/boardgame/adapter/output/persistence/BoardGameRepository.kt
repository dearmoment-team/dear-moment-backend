package kr.kro.onboarding.boardgame.adapter.output.persistence

import kr.kro.onboarding.boardgame.adapter.output.persistence.entity.BoardGameEntity
import org.springframework.data.jpa.repository.JpaRepository

interface BoardGameRepository : JpaRepository<BoardGameEntity, Long> {
}
