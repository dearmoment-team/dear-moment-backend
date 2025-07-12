package kr.kro.dearmoment.product.application.usecase.search

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kr.kro.dearmoment.common.fixture.productFixture
import kr.kro.dearmoment.like.application.port.output.GetLikePort
import kr.kro.dearmoment.product.application.dto.request.SearchProductRequest
import kr.kro.dearmoment.product.application.port.out.GetProductPort
import kr.kro.dearmoment.product.domain.model.Product
import org.springframework.data.domain.Pageable
import java.util.UUID

class ProductSearchUseCaseTest : BehaviorSpec({

    // ────────────────────────────────────────────────────────────────────────────────
    // Mocks & SUT
    // ────────────────────────────────────────────────────────────────────────────────
    val getProductPort = mockk<GetProductPort>()
    val getLikePort = mockk<GetLikePort>()
    val service = ProductSearchUseCaseImpl(getProductPort, getLikePort)

    Given("searchProducts 는") {
        val request = SearchProductRequest()
        val page = 0
        val size = 10

        When("query 와 page 정보를 전달받으면") {
            val userId = UUID.randomUUID()

            // 예: id 1, 7 두 개만 존재한다고 가정
            val products: List<Product> =
                listOf(
                    productFixture(productId = 1L),
                    productFixture(productId = 7L),
                )

            // ⚠️ pageable 객체는 Service 내부에서 새로 생성되므로 any() matcher 사용
            every {
                getProductPort.searchByCriteria(request.toQuery(), any<Pageable>())
            } returns products

            // 좋아요 조회는 이번 테스트에서 사용하지 않으므로 any() 로 가벼운 스텁
            every {
                getLikePort.findProductLikesByUserIdAndProductIds(userId, any())
            } returns emptyList()

            Then("상품들을 정상적으로 조회한다") {
                val response =
                    service.searchProducts(
                        userId = null,
                        request = request,
                        page = page,
                        size = size,
                    )

                response.content.size shouldBe products.size
                response.content[0].productId shouldBe 1L
                response.content[1].productId shouldBe 7L
            }
        }
    }
})
