package kr.kro.dearmoment.like.adapter.output.persistence

import kr.kro.dearmoment.like.application.port.output.DeleteLikePort
import kr.kro.dearmoment.like.application.port.output.GetLikePort
import kr.kro.dearmoment.like.application.port.output.SaveLikePort
import kr.kro.dearmoment.like.domain.Like
import org.springframework.stereotype.Component

@Component
class LikePersistenceAdapter(
    private val likeRepository: JpaLikeRepository,
) : SaveLikePort, DeleteLikePort, GetLikePort {
    override fun save(like: Like): Long {
        val entity = LikeEntity.from(like)
        return likeRepository.save(entity).id
    }

    override fun delete(likeId: Long) {
        likeRepository.deleteById(likeId)
    }

    override fun loadLikes(userId: Long): List<Like> =
        likeRepository.findByUserId(userId)
            .map { it.toDomain() }

    override fun existLike(
        userId: Long,
        targetId: Long,
        type: String,
    ): Boolean = likeRepository.existsByUserIdAndTargetIdAndType(userId, targetId, type)
}
