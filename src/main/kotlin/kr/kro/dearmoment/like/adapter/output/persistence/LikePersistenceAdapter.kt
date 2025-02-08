package kr.kro.dearmoment.like.adapter.output.persistence

import kr.kro.dearmoment.like.application.port.output.DeleteLikePort
import kr.kro.dearmoment.like.application.port.output.SaveLikePort
import kr.kro.dearmoment.like.domain.Like
import org.springframework.stereotype.Component

@Component
class LikePersistenceAdapter(
    private val likeRepository: JpaLikeRepository,
) : SaveLikePort, DeleteLikePort {
    override fun save(like: Like): Long {
        val entity = LikeEntity.from(like)
        return likeRepository.save(entity).id
    }

    override fun delete(likeId: Long) {
        likeRepository.deleteById(likeId)
    }
}
