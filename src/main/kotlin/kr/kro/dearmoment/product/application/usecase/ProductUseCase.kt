package kr.kro.dearmoment.product.application.usecase

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kr.kro.dearmoment.product.application.port.out.ProductEntityRetrievalPort
import kr.kro.dearmoment.product.application.port.out.ProductOptionPersistencePort
import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductOption

@Service
class ProductUseCase(
    private val productPersistencePort: ProductPersistencePort,
    private val productOptionPersistencePort: ProductOptionPersistencePort,
    private val productEntityRetrievalPort: ProductEntityRetrievalPort,
) {

    /**
     * 상품과 옵션을 저장합니다.
     */
    @Transactional
    fun saveProduct(product: Product): Product {
        val savedProduct = productPersistencePort.save(product)

        product.options.forEach { option ->
            saveOrUpdateOption(savedProduct.productId!!, option)
        }

        return savedProduct.copy(options = product.options)
    }

    /**
     * 상품 ID로 상품을 조회합니다.
     * @throws IllegalArgumentException 상품이 존재하지 않을 경우 예외 발생
     */
    fun getProductById(productId: Long): Product {
        return productPersistencePort.findById(productId)
            ?: throw IllegalArgumentException("Product with ID $productId not found")
    }

    /**
     * 상품 정보를 업데이트합니다.
     * @throws IllegalArgumentException 상품이 존재하지 않을 경우 예외 발생
     */
    @Transactional
    fun updateProduct(product: Product): Product {
        val existingProduct = productPersistencePort.findById(product.productId!!)
            ?: throw IllegalArgumentException("Product with ID ${product.productId} not found")

        val updatedProduct = existingProduct.copy(
            title = product.title,
            description = product.description,
            price = product.price,
            updatedAt = product.updatedAt
        )

        productPersistencePort.save(updatedProduct)
        modifyProductOptions(product.productId!!, product.options)

        val updatedOptions = productOptionPersistencePort.findByProduct(
            productEntityRetrievalPort.getProductById(product.productId)!!
        )
        return updatedProduct.copy(options = updatedOptions)
    }

    /**
     * 상품 옵션 추가 및 삭제를 처리합니다.
     * @throws IllegalArgumentException 상품이 존재하지 않을 경우 예외 발생
     */
    @Transactional
    fun modifyProductOptions(productId: Long?, newOptions: List<ProductOption>) {
        val productEntity = productEntityRetrievalPort.getProductById(productId)
        val existingOptions = productOptionPersistencePort.findByProduct(productEntity!!)

        // 삭제 처리
        deleteUnusedOptions(existingOptions, newOptions)

        // 추가 또는 업데이트 처리
        val processedOptions = mutableSetOf<Long?>() // 저장된 옵션 ID 추적
        newOptions.forEach { option ->
            if (isNewOrUpdatedOption(option, existingOptions) && option.optionId !in processedOptions) {
                saveOrUpdateOption(productId!!, option)
                processedOptions.add(option.optionId) // 저장된 옵션 ID 추가
            }
        }
    }

    /**
     * 기존 옵션 중 사용되지 않는 옵션 삭제
     */
    private fun deleteUnusedOptions(
        existingOptions: List<ProductOption>,
        newOptions: List<ProductOption>
    ) {
        existingOptions.filter { existingOption ->
            newOptions.none { it.optionId == existingOption.optionId }
        }.forEach { option ->
            option.optionId?.let { productOptionPersistencePort.deleteById(it) }
        }
    }

    /**
     * 새로운 옵션인지 또는 업데이트가 필요한 옵션인지 확인
     */
    private fun isNewOrUpdatedOption(
        newOption: ProductOption,
        existingOptions: List<ProductOption>
    ): Boolean {
        return newOption.optionId == null || existingOptions.none { existingOption ->
            existingOption.optionId == newOption.optionId &&
                    existingOption.name == newOption.name &&
                    existingOption.description == newOption.description &&
                    existingOption.additionalPrice == newOption.additionalPrice
        }
    }

    /**
     * 옵션 저장 또는 업데이트
     */
    private fun saveOrUpdateOption(productId: Long, option: ProductOption) {
        try {
            val updatedOption = option.copy(productId = productId)
            productOptionPersistencePort.save(updatedOption)
        } catch (e: Exception) {
            throw RuntimeException("옵션 저장 중 문제가 발생했습니다: ${e.message}", e)
        }
    }

    /**
     * 상품과 연관된 옵션을 삭제한 후 상품을 삭제합니다.
     * @throws IllegalArgumentException 상품이 존재하지 않을 경우 예외 발생
     */
    /**
     * 상품과 연관된 옵션을 삭제한 후 상품을 삭제합니다.
     * @throws IllegalArgumentException 상품이 존재하지 않을 경우 예외 발생
     */
    @Transactional
    fun deleteProduct(productId: Long) {
        // 상품 ID로 상품을 조회 (존재 여부 확인)
        val productEntity = productEntityRetrievalPort.getProductById(productId)

        // 연관된 옵션을 일괄 삭제
        productOptionPersistencePort.deleteAllByProductId(productId)

        // 상품 삭제
        productPersistencePort.deleteById(productId)
    }


}
