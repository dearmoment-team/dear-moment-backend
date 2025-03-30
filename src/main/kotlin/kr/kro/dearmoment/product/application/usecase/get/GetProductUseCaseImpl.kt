package kr.kro.dearmoment.product.application.usecase.get

import kr.kro.dearmoment.common.exception.CustomException
import kr.kro.dearmoment.common.exception.ErrorCode
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
        val product =
            getProductPort.findById(productId)
                ?: throw CustomException(ErrorCode.PRODUCT_NOT_FOUND)

        val optionIds = product.options.map { it.optionId }

        val userOptionLikes =
            userId?.let {
                getLikePort.findOptionLikesByUserIdAndOptionIds(userId, optionIds)
                    .map { it.productOptionId }.toSet()
            } ?: emptySet()

        return GetProductResponse.fromDomain(product, userOptionLikes)
    }
}
