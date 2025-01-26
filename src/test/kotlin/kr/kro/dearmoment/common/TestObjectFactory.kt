package kr.kro.dearmoment.common

import kr.kro.dearmoment.product.adapter.out.persistence.JpaProductOptionRepository
import kr.kro.dearmoment.product.adapter.out.persistence.JpaProductRepository
import kr.kro.dearmoment.product.adapter.out.persistence.ProductEntity
import kr.kro.dearmoment.product.adapter.out.persistence.ProductOptionEntity
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductOption
import java.time.LocalDateTime

class TestObjectFactory(
    private val jpaProductRepository: JpaProductRepository,
    private val jpaProductOptionRepository: JpaProductOptionRepository
) {

    fun createTestProductEntity(
        fixedNow: LocalDateTime = LocalDateTime.now(),
        userId: Long = 1L,
        title: String = "테스트 상품",
        description: String = "테스트 설명",
        price: Long = 100_000,
        typeCode: Int = 1
    ): ProductEntity {
        return ProductEntity(
            productId = null,
            userId = userId,
            title = title,
            description = description,
            price = price,
            typeCode = typeCode,
            createdAt = fixedNow,
            updatedAt = fixedNow
        )
    }

    fun createTestProductOptionDomain(
        fixedNow: LocalDateTime = LocalDateTime.now(),
        productId: Long,
        name: String = "테스트 옵션",
        additionalPrice: Long = 5_000L,
        description: String = "$name 설명"
    ): ProductOption {
        return ProductOption(
            optionId = 0L,
            name = name,
            additionalPrice = additionalPrice,
            description = description,
            productId = productId,
            createdAt = fixedNow,
            updatedAt = fixedNow
        )
    }

    fun createTestProductDomain(
        userId: Long = 1L,
        title: String = "테스트 상품",
        description: String = "테스트 설명",
        price: Long = 100_000,
        typeCode: Int = 1,
        createdAt: LocalDateTime = LocalDateTime.now(),
        updatedAt: LocalDateTime = LocalDateTime.now(),
        options: List<ProductOption> = emptyList()
    ): Product {
        return Product(
            productId = 0L,
            userId = userId,
            title = title,
            description = description,
            price = price,
            typeCode = typeCode,
            shootingTime = createdAt,
            shootingLocation = "테스트 위치",
            numberOfCostumes = 0,
            packagePartnerShops = "",
            detailedInfo = "",
            warrantyInfo = "",
            contactInfo = "",
            createdAt = createdAt,
            updatedAt = updatedAt,
            options = options
        )
    }


    fun saveProductEntity(productEntity: ProductEntity): ProductEntity {
        return jpaProductRepository.saveAndFlush(productEntity)
    }

    fun saveProductOption(option: ProductOption): ProductOption {
        val productEntity = jpaProductRepository.findById(option.productId)
            .orElseThrow { IllegalArgumentException("Product with ID ${option.productId} not found") }

        val entity = ProductOptionEntity.fromDomain(option, productEntity)
        val savedEntity = jpaProductOptionRepository.saveAndFlush(entity)
        productEntity.options.add(savedEntity)
        jpaProductRepository.saveAndFlush(productEntity)

        return savedEntity.toDomain()
    }
}
