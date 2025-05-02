package kr.kro.dearmoment.inquiry.adapter.output.persistence.product

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kr.kro.dearmoment.common.fixture.imageEntityFixture
import kr.kro.dearmoment.common.fixture.productEntityFixture
import kr.kro.dearmoment.common.fixture.productOptionEntityFixture
import kr.kro.dearmoment.common.fixture.studioEntityFixture
import kr.kro.dearmoment.inquiry.domain.CreateProductOptionInquiry
import java.util.UUID

class ProductOptionInquiryEntityTest : DescribeSpec({
    describe("ProductOptionInquiryEntity") {
        val userId = UUID.randomUUID()
        val studio = studioEntityFixture(userId, imageEntityFixture(userId))
        val product = productEntityFixture(userId, studio, 1L)
        val option = productOptionEntityFixture(product, 1L)

        val inquiry =
            CreateProductOptionInquiry(
                userId = userId,
                optionId = option.optionId,
                productId = product.productId!!,
            )
        val productOptionInquiryEntity = ProductOptionInquiryEntity.from(inquiry, option)

        it("CreateProductOptionInquiry와 ProductOptionEntity에서 유효한 ProductOptionInquiryEntity가 생성되어야 한다") {

            productOptionInquiryEntity.userId shouldBe inquiry.userId
            productOptionInquiryEntity.option shouldBe option
        }

        it("도메인 객체(ProductOptionInquiry)로 변환되어야 한다") {
            val result = productOptionInquiryEntity.toDomain()

            result.id shouldBe productOptionInquiryEntity.id
            result.userId shouldBe productOptionInquiryEntity.userId
            result.productId shouldBe option.product.productId
            result.optionName shouldBe option.name
        }

        it("product가 null일 경우 예외가 발생해야 한다") {
            val invalidProductOptionInquiryEntity =
                ProductOptionInquiryEntity(
                    id = 1L,
                    userId = UUID.randomUUID(),
                    option =
                        productOptionEntityFixture(
                            productEntityFixture(studioEntity = studioEntityFixture(userId, imageEntityFixture(userId)))
                        ),
                )

            shouldThrow<IllegalStateException> {
                invalidProductOptionInquiryEntity.toDomain()
            }
        }
    }
})
