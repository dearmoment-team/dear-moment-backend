package kr.kro.dearmoment.product.application.usecase.get

import kr.kro.dearmoment.like.application.port.output.GetLikePort
import kr.kro.dearmoment.product.application.dto.response.GetProductResponse
import kr.kro.dearmoment.product.application.port.out.GetProductPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class GetProductUseCaseImpl(
    private val getProductPort: GetProductPort,
    private val getLikePort: GetLikePort,
) : GetProductUseCase {
    @Transactional(readOnly = true)
    override fun getProductById(
        productId: Long,
        userId: UUID?,
    ): GetProductResponse {
        val product = getProductPort.findWithStudioById(productId)
        val optionIds = product.options.map { it.optionId }
        val userProductLikeId =
            userId?.let {
                getLikePort.findProductLikesByUserIdAndProductId(userId, productId)
            }?.id ?: 0L
        val userOptionLikes =
            userId?.let {
                getLikePort.findOptionLikesByUserIdAndOptionIds(userId, optionIds)
                    .associate { it.productOptionId to it.id }
            } ?: emptyMap()

        return GetProductResponse.fromDomain(product, userProductLikeId, userOptionLikes)
    }

    @Transactional(readOnly = true)
    override fun getMyProduct(userId: UUID): GetProductResponse {
        val product = getProductPort.findTopByUserId(userId)
        return GetProductResponse.fromDomain(product)
    }
}
