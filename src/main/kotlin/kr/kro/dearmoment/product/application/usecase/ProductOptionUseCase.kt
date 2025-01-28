package kr.kro.dearmoment.product.application.usecase

import kr.kro.dearmoment.product.application.port.out.ProductEntityRetrievalPort
import kr.kro.dearmoment.product.application.port.out.ProductOptionPersistencePort
import kr.kro.dearmoment.product.domain.model.ProductOption
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductOptionUseCase(
    private val productOptionPersistencePort: ProductOptionPersistencePort,
    private val productEntityRetrievalPort: ProductEntityRetrievalPort,
) {

    /**
     * ProductOption을 저장합니다.
     * - ProductEntity를 조회하여 유효성을 검사합니다.
     * - 기존 옵션과 중복되는지 확인합니다.
     * - 옵션을 저장합니다.
     */
    @Transactional
    fun saveProductOption(productOption: ProductOption): ProductOption {
        // 1. ProductEntity 조회
        val product = productEntityRetrievalPort.getProductById(productOption.productId)
            ?: throw IllegalArgumentException("Product with ID ${productOption.productId} not found")

        // 2. 기존 옵션 조회
        val existingOptions = productOptionPersistencePort.findByProduct(product)

        // 3. 중복 검증
        validateDuplicateOptionName(existingOptions.map { it.name }.toSet(), productOption.name)

        // 4. 옵션 저장
        return productOptionPersistencePort.save(productOption)
    }

    /**
     * ProductOption을 ID로 조회합니다.
     */
    @Transactional(readOnly = true)
    fun getProductOptionById(optionId: Long): ProductOption {
        return productOptionPersistencePort.findById(optionId)
    }

    /**
     * 모든 ProductOption을 조회합니다.
     */
    @Transactional(readOnly = true)
    fun getAllProductOptions(): List<ProductOption> {
        return productOptionPersistencePort.findAll()
    }

    /**
     * ProductOption을 ID로 삭제합니다.
     */
    @Transactional
    fun deleteProductOptionById(optionId: Long) {
        productOptionPersistencePort.deleteById(optionId)
    }

    /**
     * 특정 Product에 속한 모든 ProductOption을 조회합니다.
     */
    @Transactional(readOnly = true)
    fun getProductOptionsByProductId(productId: Long): List<ProductOption> {
        val product = productEntityRetrievalPort.getProductById(productId)
            ?: throw IllegalArgumentException("Product with ID $productId not found")
        return productOptionPersistencePort.findByProduct(product)
    }

    /**
     * 특정 Product에 속한 모든 ProductOption을 삭제합니다.
     */
    @Transactional
    fun deleteAllProductOptionsByProductId(productId: Long) {
        productOptionPersistencePort.deleteAllByProductId(productId)
    }

    /**
     * 특정 Product에 옵션이 존재하는지 확인합니다.
     */
    @Transactional(readOnly = true)
    fun existsProductOptions(productId: Long): Boolean {
        return productOptionPersistencePort.existsByProductId(productId)
    }

    /**
     * 옵션 이름의 중복을 검증합니다.
     */
    private fun validateDuplicateOptionName(
        existingOptionNames: Set<String>,
        newOptionName: String,
    ) {
        if (newOptionName in existingOptionNames) {
            throw IllegalArgumentException("Duplicate option name: $newOptionName")
        }
    }
}
