package kr.kro.dearmoment.product.application.usecase

import kr.kro.dearmoment.product.application.port.out.ProductEntityRetrievalPort
import kr.kro.dearmoment.product.application.port.out.ProductOptionPersistencePort
import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductOption
import org.springframework.stereotype.Service

@Service
class ProductUseCase(
    private val productPersistencePort: ProductPersistencePort,
    private val productOptionPersistencePort: ProductOptionPersistencePort,
    private val productEntityRetrievalPort: ProductEntityRetrievalPort,
) {
    /**
     * 상품과 옵션을 저장합니다.
     */
    fun saveProduct(product: Product): Product {
        // 상품 저장
        val savedProduct = productPersistencePort.save(product)

        // 옵션 저장
        product.options.forEach { option ->
            val updatedOption = option.copy(productId = savedProduct.productId)
            productOptionPersistencePort.save(updatedOption)
        }

        return savedProduct
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
    fun updateProduct(product: Product): Product {
        // 기존 상품 조회
        val existingProduct =
            productPersistencePort.findById(product.productId)
                ?: throw IllegalArgumentException("Product with ID ${product.productId} not found")

        // 업데이트된 옵션 반영
        val updatedOptions =
            product.options.map { option ->
                val updatedOption = option.copy(productId = product.productId)
                productOptionPersistencePort.save(updatedOption)
                updatedOption
            }

        // 상품 업데이트
        val updatedProduct =
            existingProduct.copy(
                title = product.title,
                description = product.description,
                price = product.price,
                updatedAt = product.updatedAt,
                options = updatedOptions,
            )
        return productPersistencePort.save(updatedProduct)
    }

    /**
     * 상품 옵션 추가 및 삭제를 처리합니다.
     * @throws IllegalArgumentException 상품이 존재하지 않을 경우 예외 발생
     */
    fun modifyProductOptions(
        productId: Long,
        newOptions: List<ProductOption>,
    ) {
        val productEntity = productEntityRetrievalPort.getProductEntityById(productId)
        val existingOptions = productOptionPersistencePort.findByProduct(productEntity)

        // 기존 옵션 중 삭제 대상 식별 및 삭제
        existingOptions.forEach { option ->
            if (newOptions.none { it.optionId == option.optionId && it.name == option.name }) {
                productOptionPersistencePort.deleteById(option.optionId)
            }
        }

        // 새로운 옵션 중 기존 옵션에 없는 것만 저장
        val optionsToSave = newOptions.filter { newOption ->
            existingOptions.none {
                it.optionId == newOption.optionId && it.name == newOption.name
            }
        }

        optionsToSave.forEach { option ->
            val updatedOption = option.copy(productId = productId)
            productOptionPersistencePort.save(updatedOption)
        }
    }


}
