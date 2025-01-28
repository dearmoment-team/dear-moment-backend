package kr.kro.dearmoment.product.adapter.out.persistence

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kr.kro.dearmoment.product.domain.model.Product
import java.time.LocalDateTime

class ProductEntityTest : StringSpec({

    "ProductEntity should convert from domain model correctly" {
        val product = Product(
            productId = 1L,
            userId = 123L,
            title = "Test Product",
            description = "This is a test product",
            price = 1000L,
            typeCode = 1,
            shootingTime = LocalDateTime.of(2023, 1, 1, 10, 0, 0),
            shootingLocation = "Test Location",
            numberOfCostumes = 5,
            packagePartnerShops = "Test Shop",
            detailedInfo = "Detailed information",
            warrantyInfo = "1 year warranty",
            contactInfo = "test@example.com",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            images = listOf("image1.jpg", "image2.jpg")
        )

        val productEntity = ProductEntity.fromDomain(product)

        productEntity.productId shouldBe product.productId
        productEntity.userId shouldBe product.userId
        productEntity.title shouldBe product.title
        productEntity.description shouldBe product.description
        productEntity.price shouldBe product.price
        productEntity.typeCode shouldBe product.typeCode
        productEntity.shootingTime shouldBe product.shootingTime
        productEntity.shootingLocation shouldBe product.shootingLocation
        productEntity.numberOfCostumes shouldBe product.numberOfCostumes
        productEntity.packagePartnerShops shouldBe product.packagePartnerShops
        productEntity.detailedInfo shouldBe product.detailedInfo
        productEntity.warrantyInfo shouldBe product.warrantyInfo
        productEntity.contactInfo shouldBe product.contactInfo
        productEntity.images shouldBe product.images
    }

    "ProductEntity should convert to domain model correctly" {
        val productEntity = ProductEntity(
            productId = 1L,
            userId = 123L,
            title = "Test Product",
            description = "This is a test product",
            price = 1000L,
            typeCode = 1,
            shootingTime = LocalDateTime.of(2023, 1, 1, 10, 0, 0),
            shootingLocation = "Test Location",
            numberOfCostumes = 5,
            packagePartnerShops = "Test Shop",
            detailedInfo = "Detailed information",
            warrantyInfo = "1 year warranty",
            contactInfo = "test@example.com",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            images = listOf("image1.jpg", "image2.jpg")
        )

        val product = productEntity.toDomain()

        product.productId shouldBe productEntity.productId
        product.userId shouldBe productEntity.userId
        product.title shouldBe productEntity.title
        product.description shouldBe productEntity.description
        product.price shouldBe productEntity.price
        product.typeCode shouldBe productEntity.typeCode
        product.shootingTime shouldBe productEntity.shootingTime
        product.shootingLocation shouldBe productEntity.shootingLocation
        product.numberOfCostumes shouldBe productEntity.numberOfCostumes
        product.packagePartnerShops shouldBe productEntity.packagePartnerShops
        product.detailedInfo shouldBe productEntity.detailedInfo
        product.warrantyInfo shouldBe productEntity.warrantyInfo
        product.contactInfo shouldBe productEntity.contactInfo
        product.images shouldBe productEntity.images
    }
})
