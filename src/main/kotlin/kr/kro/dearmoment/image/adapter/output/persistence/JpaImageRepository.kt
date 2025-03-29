package kr.kro.dearmoment.image.adapter.output.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface JpaImageRepository : JpaRepository<ImageEntity, Long> {
    fun findAllByUserId(userId: UUID): List<ImageEntity>
}
