package kr.kro.dearmoment.studio.adapter.output.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface StudioJpaRepository : JpaRepository<StudioEntity, Long> {
    fun findByUserId(id: UUID): StudioEntity?
}
