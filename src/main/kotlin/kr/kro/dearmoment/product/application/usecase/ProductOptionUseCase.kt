package kr.kro.dearmoment.product.application.usecase

import kr.kro.dearmoment.product.application.port.out.ProductEntityRetrievalPort
import kr.kro.dearmoment.product.application.port.out.ProductOptionPersistencePort
import kr.kro.dearmoment.product.domain.model.ProductOption
import org.springframework.stereotype.Service

@Service
class ProductOptionUseCase(
    private val productOptionPersistencePort: ProductOptionPersistencePort,
    private val productEntityRetrievalPort: ProductEntityRetrievalPort
) {

    fun saveProductOption(productOption: ProductOption) {
        // 1. ProductEntity 조회
        val productEntity = productEntityRetrievalPort.getProductEntityById(productOption.productId)

        // 2. 기존 옵션 조회
        val existingOptions = productOptionPersistencePort.findByProduct(productEntity)

        // 3. 중복 검증
        validateDuplicateOptionName(existingOptions.map { it.name }.toSet(), productOption.name)

        // 4. 옵션 저장
        productOptionPersistencePort.save(productOption)
    }

    private fun validateDuplicateOptionName(
        existingOptionNames: Set<String>,
        newOptionName: String
    ) {
        if (newOptionName in existingOptionNames) {
            throw IllegalArgumentException("Duplicate option name: $newOptionName")
        }
    }
}