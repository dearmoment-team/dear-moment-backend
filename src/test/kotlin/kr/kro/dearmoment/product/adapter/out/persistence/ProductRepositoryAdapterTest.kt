package kr.kro.dearmoment.product.adapter.out.persistence

import io.kotest.core.spec.style.StringSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductOption
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import java.time.LocalDateTime

@DataJpaTest
@Import(ProductRepositoryAdapter::class)
class ProductRepositoryAdapterTest : StringSpec() {
    @Autowired
    private lateinit var productRepositoryAdapter: ProductRepositoryAdapter

    init {
        "상품 + 옵션을 저장 후 다시 조회해본다" {
            // given
            val now = LocalDateTime.now()

            val options =
                listOf(
                    ProductOption(
                        name = "익스프레스 서비스",
                        additionalPrice = 80000,
                        description = "최대 1주~3주 내 보정본 전달",
                    ),
                    ProductOption(
                        name = "세부 보정 추가",
                        additionalPrice = 10000,
                        description = "세부 보정 15장 제공",
                    ),
                )

            val product =
                Product(
                    productId = 0L,
                    userId = 1L,
                    title = "옵션 테스트 상품",
                    description = "옵션 포함 테스트 설명",
                    price = 100000,
                    typeCode = 1,
                    createdAt = now,
                    updatedAt = now,
                    options = options,
                )

            // when
            val savedProduct = productRepositoryAdapter.save(product)
            val foundProduct = productRepositoryAdapter.findById(savedProduct.productId)

            // then
            foundProduct.productId shouldBe savedProduct.productId
            foundProduct.options?.shouldHaveSize(2)
            foundProduct.options?.get(0)?.name shouldBe "익스프레스 서비스"
            foundProduct.options?.get(1)?.additionalPrice shouldBe 10000
        }
    }

    override fun extensions() = listOf(SpringExtension)
}
