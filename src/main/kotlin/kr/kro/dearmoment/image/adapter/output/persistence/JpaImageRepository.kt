package kr.kro.dearmoment.image.adapter.output.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface JpaImageRepository : JpaRepository<ImageEntity, Long> {
    fun findAllByUserId(userId: Long): List<ImageEntity>

    @Modifying
    @Query("UPDATE ImageEntity i SET i.url = :url WHERE i.id = :id")
    fun updateImageUrl(
        @Param("id") imageId: Long,
        @Param("url") imageUrl: String,
    ): Int
}
