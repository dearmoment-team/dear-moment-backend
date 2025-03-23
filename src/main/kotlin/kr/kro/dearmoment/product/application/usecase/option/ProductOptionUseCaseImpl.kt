package kr.kro.dearmoment.product.application.usecase.option

import kr.kro.dearmoment.common.exception.CustomException
import kr.kro.dearmoment.common.exception.ErrorCode
import kr.kro.dearmoment.product.application.dto.request.CreateProductOptionRequest
import kr.kro.dearmoment.product.application.dto.request.UpdateProductOptionRequest
import kr.kro.dearmoment.product.application.dto.response.ProductOptionResponse
import kr.kro.dearmoment.product.application.port.out.GetProductOptionPort
import kr.kro.dearmoment.product.application.port.out.GetProductPort
import kr.kro.dearmoment.product.application.port.out.ProductOptionPersistencePort
import kr.kro.dearmoment.product.domain.model.OptionType
import kr.kro.dearmoment.product.domain.model.PartnerShop
import kr.kro.dearmoment.product.domain.model.PartnerShopCategory
import kr.kro.dearmoment.product.domain.model.Product
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductOptionUseCaseImpl(
    private val productOptionPersistencePort: ProductOptionPersistencePort,
    private val getProductOptionPort: GetProductOptionPort,
    private val getProductPort: GetProductPort,
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
        val option = getProductOptionPort.findById(optionId)
        return ProductOptionResponse.fromDomain(option)
    }

    @Transactional(readOnly = true)
    override fun getAllProductOptions(): List<ProductOptionResponse> {
        return getProductOptionPort.findAll().map { ProductOptionResponse.fromDomain(it) }
    }

    @Transactional
    override fun deleteProductOptionById(optionId: Long) {
        productOptionPersistencePort.deleteById(optionId)
    }

    @Transactional(readOnly = true)
    override fun getProductOptionsByProductId(productId: Long): List<ProductOptionResponse> {
        return getProductOptionPort.findByProductId(productId)
            .map { ProductOptionResponse.fromDomain(it) }
    }

    @Transactional
    override fun deleteAllProductOptionsByProductId(productId: Long) {
        productOptionPersistencePort.deleteAllByProductId(productId)
    }

    @Transactional(readOnly = true)
    override fun existsProductOptions(productId: Long): Boolean {
        return getProductOptionPort.existsByProductId(productId)
    }

    @Transactional
    override fun synchronizeOptions(
        existingProduct: Product,
        requestOptions: List<UpdateProductOptionRequest>,
    ) {
        val existingOptions = getProductOptionPort.findByProductId(existingProduct.productId)
        val existingOptionMap = existingOptions.associateBy { it.optionId }

        requestOptions.forEach { dto ->
            if (dto.optionId != null && existingOptionMap.containsKey(dto.optionId)) {
                val existingOpt =
                    existingOptionMap[dto.optionId]
                        ?: throw CustomException(ErrorCode.OPTION_NOT_FOUND)
                val updatedOpt =
                    existingOpt.copy(
                        name = dto.name,
                        optionType = OptionType.valueOf(dto.optionType),
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
                                PartnerShop(
                                    category = PartnerShopCategory.valueOf(it.category),
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

    @Transactional
    override fun saveOrUpdateProductOption(
        productId: Long,
        request: UpdateProductOptionRequest?,
    ): ProductOptionResponse? {
        val product = getProductOrThrow(productId)
        if (request == null) {
            return null
        }
        return if (request.optionId == null) {
            val newOption = UpdateProductOptionRequest.toDomain(request, productId)
            val savedOption = productOptionPersistencePort.save(newOption, product)
            ProductOptionResponse.fromDomain(savedOption)
        } else {
            // 기존 옵션 업데이트
            val existingOption = getProductOptionPort.findById(request.optionId)

            val updatedOption =
                existingOption.copy(
                    name = request.name,
                    optionType = OptionType.valueOf(request.optionType),
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
                            PartnerShop(
                                category = PartnerShopCategory.valueOf(it.category),
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
        return getProductPort.findById(productId)
            ?: throw CustomException(ErrorCode.PRODUCT_NOT_FOUND)
    }

    private fun validateDuplicateOption(
        productId: Long,
        name: String,
    ) {
        if (getProductOptionPort.existsByProductIdAndName(productId, name)) {
            throw CustomException(ErrorCode.DUPLICATE_OPTION_NAME)
        }
    }
}
