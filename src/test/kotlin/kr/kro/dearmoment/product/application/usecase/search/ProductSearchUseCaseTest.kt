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
import org.springframework.data.domain.PageRequest
import java.util.UUID

class ProductSearchUseCaseTest : BehaviorSpec({
    val getProductPort = mockk<GetProductPort>()
    val getLikePort = mockk<GetLikePort>()
    val service = ProductSearchUseCaseImpl(getProductPort, getLikePort)

    Given("searchProducts는") {
        val request = SearchProductRequest()
        val pageable = PageRequest.of(0, 10)

        When("query, page 정보를 전달받으면") {
            val userId = UUID.randomUUID()
            val products = listOf(productFixture(), productFixture()) // 리스트로 데이터 생성
            val productsPage: List<Product> = products

            every { getProductPort.searchByCriteria(request.toQuery(), pageable) } returns productsPage
            every { getLikePort.findProductLikesByUserIdAndProductIds(userId, products.map { it.productId }) } returns emptyList()

            Then("상품들을 조회한다.") {
                val response = service.searchProducts(null, request, pageable.pageNumber, pageable.pageSize)

                response.content.size shouldBe productsPage.size
            }
        }
    }
})
