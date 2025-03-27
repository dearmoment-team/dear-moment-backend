package kr.kro.dearmoment.product.application.usecase.search

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.kro.dearmoment.common.dto.PagedResponse
import kr.kro.dearmoment.image.domain.Image
import kr.kro.dearmoment.product.application.dto.response.ProductResponse
import kr.kro.dearmoment.product.application.port.out.GetProductPort
import kr.kro.dearmoment.product.application.usecase.util.PaginationUtil
import kr.kro.dearmoment.product.domain.model.CameraType
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductType
import kr.kro.dearmoment.product.domain.model.RetouchStyle
import kr.kro.dearmoment.product.domain.model.ShootingPlace
import kr.kro.dearmoment.product.domain.model.ShootingSeason
import java.time.LocalDateTime
import java.util.UUID

class ProductSearchUseCaseTest : BehaviorSpec({

    // Dummy user IDs
    val dummyUserId1 = UUID.fromString("11111111-1111-1111-1111-111111111111")
    val dummyUserId2 = UUID.fromString("22222222-2222-2222-2222-222222222222")

    // Mock 객체 설정 (테스트 전체에서 공유됨)
    val getProductPort = mockk<GetProductPort>()
    val paginationUtil = mockk<PaginationUtil>()

    // 테스트 대상 UseCase
    val useCase =
        ProductSearchUseCaseImpl(
            getProductPort = getProductPort,
            paginationUtil = paginationUtil,
        )

    // 테스트용 더미 이미지 (userId: dummyUserId1)
    val dummyImage =
        Image(
            imageId = 1L,
            userId = dummyUserId1,
            fileName = "dummy.jpg",
            url = "http://test.com/dummy.jpg",
        )

    // 서브 이미지는 4장이어야 함
    val fourSubImages = List(4) { dummyImage }

    val product1 =
        Product(
            productId = 1L,
            userId = dummyUserId1,
            productType = ProductType.WEDDING_SNAP,
            shootingPlace = ShootingPlace.JEJU,
            title = "First Product",
            description = "Desc1",
            availableSeasons = setOf(ShootingSeason.YEAR_2025_FIRST_HALF),
            cameraTypes = setOf(CameraType.FILM),
            retouchStyles = setOf(RetouchStyle.NATURAL),
            mainImage = dummyImage,
            subImages = fourSubImages,
            additionalImages = listOf(dummyImage),
            detailedInfo = "Info1",
            contactInfo = "Contact1",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            options = emptyList(),
        )

    val product2 =
        Product(
            productId = 2L,
            userId = dummyUserId2,
            productType = ProductType.WEDDING_SNAP,
            shootingPlace = ShootingPlace.JEJU,
            title = "Second Product",
            description = "Desc2",
            availableSeasons = setOf(ShootingSeason.YEAR_2025_FIRST_HALF),
            cameraTypes = setOf(CameraType.FILM),
            retouchStyles = setOf(RetouchStyle.NATURAL),
            mainImage = dummyImage,
            subImages = fourSubImages,
            additionalImages = listOf(dummyImage),
            detailedInfo = "Info2",
            contactInfo = "Contact2",
            createdAt = LocalDateTime.now().minusDays(1),
            updatedAt = LocalDateTime.now(),
            options = emptyList(),
        )

    val dummyList = listOf(product1, product2)

    // 페이징 테스트용 파라미터
    val page = 0
    val size = 10

    // 페이징 유틸이 반환할 더미 PagedResponse
    val dummyPagedResponse =
        PagedResponse(
            content =
                listOf(
                    ProductResponse.fromDomain(product1),
                    ProductResponse.fromDomain(product2),
                ),
            page = page,
            size = size,
            totalElements = 2,
            totalPages = 1,
        )

    Given("searchProducts 메서드가 호출되었을 때") {

        When("title, productType, shootingPlace가 주어지고 sortBy가 null인 경우") {
            every {
                getProductPort.searchByCriteria("First", "WEDDING_SNAP", "JEJU", null)
            } returns listOf(product1)

            every { paginationUtil.createPagedResponse(any(), page, size) } returns
                dummyPagedResponse.copy(
                    content = listOf(ProductResponse.fromDomain(product1)),
                    totalElements = 1,
                    totalPages = 1,
                )

            val result = useCase.searchProducts("First", "WEDDING_SNAP", "JEJU", null, page, size)

            Then("상품 목록을 페이징 처리하여 반환한다") {
                result.content.size shouldBe 1
                result.content[0].title shouldBe "First Product"

                verify(exactly = 1) {
                    getProductPort.searchByCriteria("First", "WEDDING_SNAP", "JEJU", null)
                }
                verify(exactly = 1) {
                    paginationUtil.createPagedResponse(match { it.size == 1 }, page, size)
                }
            }
        }

        When("sortBy = created-desc 로 검색할 경우") {
            every {
                getProductPort.searchByCriteria(null, null, null, "created-desc")
            } returns dummyList

            // created-desc 일 때 product2 → product1 순으로 정렬
            every {
                paginationUtil.createPagedResponse(listOf(product2, product1), page, size)
            } returns
                dummyPagedResponse.copy(
                    content =
                        listOf(
                            ProductResponse.fromDomain(product2),
                            ProductResponse.fromDomain(product1),
                        ),
                    totalElements = 2,
                    totalPages = 1,
                )

            val result = useCase.searchProducts(null, null, null, "created-desc", page, size)

            Then("검색 결과를 productId 역순으로 정렬한 뒤 페이징 처리한다") {
                result.content.size shouldBe 2
                result.content[0].title shouldBe "Second Product"
                result.content[1].title shouldBe "First Product"

                verify(exactly = 1) {
                    getProductPort.searchByCriteria(null, null, null, "created-desc")
                }
                verify(exactly = 1) {
                    paginationUtil.createPagedResponse(listOf(product2, product1), page, size)
                }
            }
        }
    }

    Given("getMainPageProducts 메서드가 호출되었을 때") {

        // 이전 When 블록들의 목 호출이 누적되어 있으므로, 초기화해준다.
        clearMocks(getProductPort, paginationUtil)

        When("전체 상품 목록이 존재하는 경우") {
            every { getProductPort.findAll() } returns dummyList

            // findAll() 결과를 역순 정렬 (product2, product1)
            every {
                paginationUtil.createPagedResponse(listOf(product2, product1), page, size)
            } returns
                dummyPagedResponse.copy(
                    content =
                        listOf(
                            ProductResponse.fromDomain(product2),
                            ProductResponse.fromDomain(product1),
                        ),
                    totalElements = 2,
                    totalPages = 1,
                )

            val result = useCase.getMainPageProducts(page, size)

            Then("전체 상품을 최신 순으로 정렬하여 페이징 처리한다") {
                result.content.size shouldBe 2
                result.content[0].title shouldBe "Second Product"
                result.content[1].title shouldBe "First Product"

                verify(exactly = 1) { getProductPort.findAll() }
                verify(exactly = 1) {
                    paginationUtil.createPagedResponse(listOf(product2, product1), page, size)
                }
            }
        }
    }
})
