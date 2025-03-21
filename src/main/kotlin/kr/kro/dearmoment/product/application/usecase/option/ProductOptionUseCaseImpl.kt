package kr.kro.dearmoment.product.application.usecase.option

import kr.kro.dearmoment.product.application.dto.request.CreateProductOptionRequest
import kr.kro.dearmoment.product.application.dto.request.UpdateProductOptionRequest
import kr.kro.dearmoment.product.application.dto.response.ProductOptionResponse
import kr.kro.dearmoment.product.application.port.out.ProductOptionPersistencePort
import kr.kro.dearmoment.product.application.port.out.ProductPersistencePort
import kr.kro.dearmoment.product.domain.model.Product
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductOptionUseCaseImpl(
    private val productOptionPersistencePort: ProductOptionPersistencePort,
    private val productPersistencePort: ProductPersistencePort,
) : ProductOptionUseCase {
    @Transactional
    override fun saveProductOption(
        productId: Long,
        request: CreateProductOptionRequest,
    ): ProductOptionResponse {
        val product = getProductOrThrow(productId)
        validateDuplicateOption(productId, request.name)

        val option = CreateProductOptionRequest.toDomain(request, productId)
        val savedOption = productOptionPersistencePort.save(option, product)
        return ProductOptionResponse.fromDomain(savedOption)
    }

    @Transactional(readOnly = true)
    override fun getProductOptionById(optionId: Long): ProductOptionResponse {
        return ProductOptionResponse.fromDomain(productOptionPersistencePort.findById(optionId))
    }

    @Transactional(readOnly = true)
    override fun getAllProductOptions(): List<ProductOptionResponse> {
        return productOptionPersistencePort.findAll().map { ProductOptionResponse.fromDomain(it) }
    }

    @Transactional
    override fun deleteProductOptionById(optionId: Long) {
        productOptionPersistencePort.deleteById(optionId)
    }

    @Transactional(readOnly = true)
    override fun getProductOptionsByProductId(productId: Long): List<ProductOptionResponse> {
        return productOptionPersistencePort.findByProductId(productId)
            .map { ProductOptionResponse.fromDomain(it) }
    }

    @Transactional
    override fun deleteAllProductOptionsByProductId(productId: Long) {
        productOptionPersistencePort.deleteAllByProductId(productId)
    }

    @Transactional(readOnly = true)
    override fun existsProductOptions(productId: Long): Boolean {
        return productOptionPersistencePort.existsByProductId(productId)
    }

    /**
     * 기존 옵션과 요청된 옵션들을 비교하여 업데이트, 신규 생성을 수행한다.
     */
    @Transactional
    override fun synchronizeOptions(
        existingProduct: Product,
        requestOptions: List<UpdateProductOptionRequest>,
    ) {
        val existingOptions = productOptionPersistencePort.findByProductId(existingProduct.productId)
        val existingOptionMap = existingOptions.associateBy { it.optionId }

        // 요청 DTO에 대해 업데이트 또는 신규 생성 처리 (삭제 로직은 제거)
        requestOptions.forEach { dto ->
            if (dto.optionId != null && existingOptionMap.containsKey(dto.optionId)) {
                val existingOpt =
                    existingOptionMap[dto.optionId]
                        ?: throw IllegalStateException("Option with id ${dto.optionId} is missing")
                val updatedOpt =
                    existingOpt.copy(
                        name = dto.name,
                        optionType = kr.kro.dearmoment.product.domain.model.OptionType.valueOf(dto.optionType),
                        discountAvailable = dto.discountAvailable,
                        originalPrice = dto.originalPrice,
                        discountPrice = dto.discountPrice,
                        description = dto.description ?: "",
                        costumeCount = dto.costumeCount,
                        shootingLocationCount = dto.shootingLocationCount,
                        shootingHours = dto.shootingHours,
                        shootingMinutes = dto.shootingMinutes,
                        retouchedCount = dto.retouchedCount,
                        originalProvided = dto.originalProvided,
                        partnerShops =
                            dto.partnerShops.map {
                                kr.kro.dearmoment.product.domain.model.PartnerShop(
                                    category = kr.kro.dearmoment.product.domain.model.PartnerShopCategory.valueOf(it.category),
                                    name = it.name,
                                    link = it.link,
                                )
                            },
                    )
                productOptionPersistencePort.save(updatedOpt, existingProduct)
            } else {
                val newOpt = UpdateProductOptionRequest.toDomain(dto, existingProduct.productId)
                productOptionPersistencePort.save(newOpt, existingProduct)
            }
        }
    }

    /**
     * - Path Parameter로 상품 id를 받고,
     * - 요청 DTO(request)가 없으면 아무 작업도 하지 않으며,
     * - 요청 DTO의 optionId가 null이면 신규 옵션을 추가하고,
     * - optionId가 있으면 기존 옵션을 업데이트한다.
     */
    @Transactional
    fun saveOrUpdateProductOption(
        productId: Long,
        request: UpdateProductOptionRequest?,
    ): ProductOptionResponse? {
        val product = getProductOrThrow(productId)
        if (request == null) {
            return null
        }
        return if (request.optionId == null) {
            // 신규 옵션 추가
            val newOption = UpdateProductOptionRequest.toDomain(request, productId)
            val savedOption = productOptionPersistencePort.save(newOption, product)
            ProductOptionResponse.fromDomain(savedOption)
        } else {
            // 기존 옵션 업데이트
            val existingOption =
                productOptionPersistencePort.findById(request.optionId)
            val updatedOption =
                existingOption.copy(
                    name = request.name,
                    optionType = kr.kro.dearmoment.product.domain.model.OptionType.valueOf(request.optionType),
                    discountAvailable = request.discountAvailable,
                    originalPrice = request.originalPrice,
                    discountPrice = request.discountPrice,
                    description = request.description ?: "",
                    costumeCount = request.costumeCount,
                    shootingLocationCount = request.shootingLocationCount,
                    shootingHours = request.shootingHours,
                    shootingMinutes = request.shootingMinutes,
                    retouchedCount = request.retouchedCount,
                    originalProvided = request.originalProvided,
                    partnerShops =
                        request.partnerShops.map {
                            kr.kro.dearmoment.product.domain.model.PartnerShop(
                                category = kr.kro.dearmoment.product.domain.model.PartnerShopCategory.valueOf(it.category),
                                name = it.name,
                                link = it.link,
                            )
                        },
                )
            productOptionPersistencePort.save(updatedOption, product)
            ProductOptionResponse.fromDomain(updatedOption)
        }
    }

    private fun getProductOrThrow(productId: Long): Product {
        return productPersistencePort.findById(productId)
            ?: throw IllegalArgumentException("Product not found: $productId")
    }

    private fun validateDuplicateOption(
        productId: Long,
        name: String,
    ) {
        require(
            !productOptionPersistencePort.existsByProductIdAndName(productId, name),
        ) { "Duplicate option name: $name" }
    }
}
