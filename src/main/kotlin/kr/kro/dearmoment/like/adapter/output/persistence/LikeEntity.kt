package kr.kro.dearmoment.like.adapter.output.persistence

import Auditable
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import kr.kro.dearmoment.like.domain.Like
import kr.kro.dearmoment.like.domain.LikeType

@Entity
@Table(name = "likes")
class LikeEntity(
    @Id
    @Column(name = "like_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
    @Column(nullable = false)
    val userId: Long,
    @Column(nullable = false)
    val targetId: Long,
    @Column(nullable = false)
    val type: String,
) : Auditable() {
    companion object {
        fun from(domain: Like) =
            LikeEntity(
                userId = domain.userId,
                targetId = domain.targetId,
                type = domain.type.value,
            )

        fun toDomain(entity: LikeEntity) =
            Like(
                id = entity.id,
                userId = entity.userId,
                targetId = entity.targetId,
                type = LikeType.from(entity.type),
            )
    }
}
