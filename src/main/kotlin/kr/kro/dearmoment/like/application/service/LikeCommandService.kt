package kr.kro.dearmoment.like.application.service

import kr.kro.dearmoment.like.application.command.SaveLikeCommand
import kr.kro.dearmoment.like.application.command.UnlikeProductCommand
import kr.kro.dearmoment.like.application.dto.LikeResponse
import kr.kro.dearmoment.like.application.port.input.LikeUseCase
import kr.kro.dearmoment.like.application.port.output.DeleteLikePort
import kr.kro.dearmoment.like.application.port.output.SaveLikePort
import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class LikeCommandService(
    private val saveLikePort: SaveLikePort,
    private val deleteLikePort: DeleteLikePort,
    private val productPersistencePort: ProductPersistencePort,
) : LikeUseCase {
    override fun productLike(command: SaveLikeCommand): LikeResponse {
        val like = command.toStudioLikeDomain()
        val savedLikeId = saveLikePort.saveProductLike(like)

        productPersistencePort.increaseLikeCount(like.productId)

        return LikeResponse(savedLikeId)
    }

    override fun productOptionsLike(command: SaveLikeCommand): LikeResponse {
        val like = command.toProductOptionLikeDomain()
        val savedLikeId = saveLikePort.saveProductOptionLike(like)
        return LikeResponse(savedLikeId)
    }

    override fun productUnlike(command: UnlikeProductCommand) {
        deleteLikePort.deleteProductLike(command.likeId)
        productPersistencePort.decreaseLikeCount(command.productId)
    }

    override fun productOptionUnlike(likeId: Long): Unit = deleteLikePort.deleteProductOptionLike(likeId)
}
