package kr.kro.dearmoment.product.adapter.out.persistence

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kr.kro.dearmoment.product.domain.model.PartnerShop
import kr.kro.dearmoment.product.domain.model.Product
import java.time.LocalDateTime

class ProductEntityTest : StringSpec({

    "ProductEntity는 도메인 모델에서 올바르게 변환되어야 한다" {
        val fixedDateTime = LocalDateTime.of(2023, 1, 1, 10, 0, 0)
        val partnerShops =
            listOf(
                PartnerShop("상점1", "http://shop1.com"),
                PartnerShop("상점2", "http://shop2.com"),
            )

        val product =
            Product(
                productId = 1L,
                userId = 123L,
                title = "테스트 제품",
                description = "이것은 테스트 제품입니다",
                price = 1000L,
                typeCode = 1,
                shootingTime = fixedDateTime,
                shootingLocation = "테스트 장소",
                numberOfCostumes = 5,
                partnerShops = partnerShops,
                detailedInfo = "상세 정보",
                warrantyInfo = "1년 보증",
                contactInfo = "test@example.com",
                createdAt = fixedDateTime,
                updatedAt = fixedDateTime,
                images = listOf("image1.jpg", "image2.jpg"),
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
        productEntity.partnerShops.map { it.name } shouldContainExactly partnerShops.map { it.name }
        productEntity.partnerShops.map { it.link } shouldContainExactly partnerShops.map { it.link }
        productEntity.detailedInfo shouldBe product.detailedInfo
        productEntity.warrantyInfo shouldBe product.warrantyInfo
        productEntity.contactInfo shouldBe product.contactInfo

        // 여기서 createdDate / updateDate를 도메인 시간과 비교
        productEntity.createdDate shouldBe product.createdAt
        productEntity.updateDate shouldBe product.updatedAt
        productEntity.images shouldContainExactly product.images
    }

    "ProductEntity는 도메인 모델로 올바르게 변환되어야 한다" {
        val fixedDateTime = LocalDateTime.of(2023, 1, 1, 10, 0, 0)
        val partnerShops =
            listOf(
                PartnerShopEmbeddable("상점1", "http://shop1.com"),
                PartnerShopEmbeddable("상점2", "http://shop2.com"),
            )

        val productEntity =
            ProductEntity(
                productId = 1L,
                userId = 123L,
                title = "테스트 제품",
                description = "이것은 테스트 제품입니다",
                price = 1000L,
                typeCode = 1,
                shootingTime = fixedDateTime,
                shootingLocation = "테스트 장소",
                numberOfCostumes = 5,
                partnerShops = partnerShops,
                detailedInfo = "상세 정보",
                warrantyInfo = "1년 보증",
                contactInfo = "test@example.com",
                images = listOf("image1.jpg", "image2.jpg"),
            )

        // 여기서 createdDate / updateDate == null (DB 저장 전)일 수 있음
        productEntity.createdDate shouldBe null
        productEntity.updateDate shouldBe null

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
        product.partnerShops.map { it.name } shouldContainExactly partnerShops.map { it.name }
        product.partnerShops.map { it.link } shouldContainExactly partnerShops.map { it.link }
        product.detailedInfo shouldBe productEntity.detailedInfo
        product.warrantyInfo shouldBe productEntity.warrantyInfo
        product.contactInfo shouldBe productEntity.contactInfo

        // 엔티티가 null이므로 도메인도 null
        product.createdAt shouldBe null
        product.updatedAt shouldBe null
        product.images shouldContainExactly productEntity.images
    }
})
