package kr.kro.dearmoment.product.application.usecase.delete

import kr.kro.dearmoment.common.exception.CustomException
import kr.kro.dearmoment.common.exception.ErrorCode
import kr.kro.dearmoment.product.application.port.out.GetProductOptionPort
import kr.kro.dearmoment.product.application.port.out.GetProductPort
import kr.kro.dearmoment.product.application.port.out.ProductOptionPersistencePort
import kr.kro.dearmoment.product.domain.model.option.ProductOption
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class DeleteProductOptionUseCaseImpl(
    private val productOptionPersistencePort: ProductOptionPersistencePort,
    private val getProductOptionPort: GetProductOptionPort,
    private val getProductPort: GetProductPort,
) : DeleteProductOptionUseCase {
    @Transactional
    override fun deleteOption(
        userId: UUID,
        productId: Long,
        optionId: Long,
    ) {
        // 1) 옵션 조회
        val option: ProductOption = getProductOptionPort.findById(optionId)

        // 2) 해당 옵션이 지정된 상품에 속하는지 확인
        if (option.productId != productId) {
            throw CustomException(ErrorCode.INVALID_REQUEST)
        }

        // 3) 상품 조회 및 소유권 검증: 인증된 userId와 상품의 소유자가 일치해야 함
        val product =
            getProductPort.findById(productId)
                ?: throw CustomException(ErrorCode.PRODUCT_NOT_FOUND)
        if (product.userId != userId) {
            throw CustomException(ErrorCode.UNAUTHORIZED_ACCESS)
        }

        // 4) 문제없으면 옵션 삭제
        productOptionPersistencePort.deleteById(optionId)
    }
}
