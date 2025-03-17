package kr.kro.dearmoment.product.application.usecase.option

import kr.kro.dearmoment.common.exception.CustomException
import kr.kro.dearmoment.common.exception.ErrorCode
import kr.kro.dearmoment.product.application.port.out.ProductOptionPersistencePort
import kr.kro.dearmoment.product.application.usecase.delete.DeleteProductOptionUseCase
import kr.kro.dearmoment.product.domain.model.ProductOption
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DeleteProductOptionUseCaseImpl(
    private val productOptionPersistencePort: ProductOptionPersistencePort,
) : DeleteProductOptionUseCase {
    @Transactional
    override fun deleteOption(
        productId: Long,
        optionId: Long,
    ) {
        // 1) 옵션 조회
        val option: ProductOption = productOptionPersistencePort.findById(optionId)

        // 2) 해당 옵션이 해당 상품에 속하는지 확인
        if (option.productId != productId) {
            throw CustomException(
                errorCode = ErrorCode.INVALID_REQUEST,
            )
        }
        // 3) 문제없으면 삭제
        productOptionPersistencePort.deleteById(optionId)
    }
}
