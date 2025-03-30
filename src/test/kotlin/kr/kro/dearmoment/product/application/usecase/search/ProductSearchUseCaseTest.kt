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
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

class ProductSearchUseCaseTest : BehaviorSpec({
    val getProductPort = mockk<GetProductPort>()
    val getLikePort = mockk<GetLikePort>()
    val service = ProductSearchUseCaseImpl(getProductPort, getLikePort)

    Given("searchProducts는") {
        val request = SearchProductRequest()
        val pageable = PageRequest.of(0, 10)

        When("query, page 정보를 전달받으면") {
            val products = listOf(productFixture(), productFixture()) // 리스트로 데이터 생성
            val productsPage: Page<Product> = PageImpl(products, pageable, products.size.toLong()) // Page 객체 생성

            every { getProductPort.searchByCriteria(request.toQuery(), pageable) } returns productsPage

            Then("상품들을 조회한다.") {
                val response = service.searchProducts(null, request, pageable.pageNumber, pageable.pageSize)

                response.page shouldBe 0
                response.size shouldBe 10
                response.totalElements shouldBe productsPage.totalElements
                response.totalPages shouldBe productsPage.totalPages
                response.content.size shouldBe productsPage.content.size
            }
        }
    }
})
