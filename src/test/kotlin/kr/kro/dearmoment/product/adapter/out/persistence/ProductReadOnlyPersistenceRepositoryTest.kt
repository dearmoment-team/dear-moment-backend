package kr.kro.dearmoment.product.adapter.out.persistence

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import kr.kro.dearmoment.RepositoryTest
import kr.kro.dearmoment.common.fixture.productEntityFixture
import kr.kro.dearmoment.common.fixture.productOptionEntityFixture
import kr.kro.dearmoment.common.fixture.studioEntityFixture
import kr.kro.dearmoment.product.adapter.out.persistence.sort.SortCriteria
import kr.kro.dearmoment.product.application.dto.query.SearchProductQuery
import kr.kro.dearmoment.product.application.dto.request.SearchProductRequest
import kr.kro.dearmoment.product.domain.model.CameraType
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.RetouchStyle
import kr.kro.dearmoment.product.domain.model.ShootingSeason
import kr.kro.dearmoment.product.domain.model.option.PartnerShopCategory
import kr.kro.dearmoment.product.domain.model.option.ProductOption
import kr.kro.dearmoment.studio.adapter.output.persistence.StudioEntity
import kr.kro.dearmoment.studio.adapter.output.persistence.StudioJpaRepository
import org.springframework.data.domain.PageRequest
import java.util.UUID

@RepositoryTest
class ProductReadOnlyPersistenceRepositoryTest(
    private val productRepository: JpaProductRepository,
    private val productOptionRepository: JpaProductOptionRepository,
    private val studioRepository: StudioJpaRepository,
) : DescribeSpec({
        val readAdapter = ProductReadOnlyRepository(productRepository)
        afterTest {
            productOptionRepository.deleteAll()
            productRepository.deleteAll()
            studioRepository.deleteAll()
        }

        describe("searchByCriteria()는") {
            val studios = mutableListOf<StudioEntity>()
            repeat(5) { studios.add(studioRepository.save(studioEntityFixture(userId = UUID.randomUUID()))) }

            val products = mutableListOf<Product>()
            val options = mutableListOf<ProductOption>()
            val partnerShopCategories = mutableSetOf<PartnerShopCategory>()
            val shootingSeasons = mutableSetOf<ShootingSeason>()
            val retouchStyles = mutableSetOf<RetouchStyle>()
            val cameraTypes = mutableSetOf<CameraType>()
            studios.forEach { studio ->
                repeat(2) {
                    val product = productEntityFixture(userId = studio.userId, studioEntity = studio)
                    val savedProduct = productRepository.save(product)

                    product.retouchStyles.forEach { retouchStyles.add(it) }
                    product.availableSeasons.forEach { shootingSeasons.add(it) }
                    product.cameraTypes.forEach { cameraTypes.add(it) }
                    // 상품 옵션 추가 (가격 필터링을 위해)
                    val option =
                        productOptionEntityFixture(
                            productEntity =
                                ProductEntity.fromDomain(
                                    savedProduct.toDomain(),
                                    studio,
                                ),
                        )

                    val option2 =
                        productOptionEntityFixture(
                            productEntity =
                                ProductEntity.fromDomain(
                                    savedProduct.toDomain(),
                                    studio,
                                ),
                        )

                    option.partnerShops.forEach {
                        partnerShopCategories.add(it.category!!)
                    }

                    option2.partnerShops.forEach {
                        partnerShopCategories.add(it.category!!)
                    }

                    savedProduct.options.add(option)
                    savedProduct.options.add(option2)
                    options.add(option.toDomain())
                    options.add(option2.toDomain())
                    products.add(savedProduct.toDomain())
                }
            }

            context("검색 조건이 전달되면") {
                it("상품이 조회된다.") {
                    val query =
                        SearchProductQuery(
                            minPrice = 50_000L,
                            maxPrice = 200_000L,
                            partnerShopCategories = partnerShopCategories.toList(),
                            availableSeasons = shootingSeasons.toList(),
                            cameraTypes = cameraTypes.toList(),
                            retouchStyles = retouchStyles.toList(),
                            sortBy = SortCriteria.POPULAR,
                        )

                    val pageable = PageRequest.of(0, 10)
//                    val startTime = System.nanoTime()
                    val result = readAdapter.searchByCriteria(query, pageable)
//                    val endTime = System.nanoTime()
//                    val duration = (endTime - startTime) / 1_000_000 // Convert nanoseconds to milliseconds
//                    println("JPA Query Execution Time: ${duration}ms")

                    val content = result.content
                    content.forEach { product ->
                        product.options.forEach { option ->
                            option.discountPrice shouldBeGreaterThanOrEqualTo query.minPrice
                            option.discountPrice shouldBeLessThanOrEqualTo query.maxPrice

                            option.partnerShops.forEach { partnerShop ->
                                partnerShop.category shouldBeIn query.partnerShopCategories
                            }
                        }

                        product.availableSeasons.forEach { season -> season shouldBeIn query.availableSeasons }
                        product.cameraTypes.forEach { cameraType -> cameraType shouldBeIn query.cameraTypes }
                        product.retouchStyles.forEach { retouchStyle -> retouchStyle shouldBeIn query.retouchStyles }
                    }
                }
            }

            context("검색 조건이 비어 있어도") {
                it("상품이 조회된다.") {

                    val query = SearchProductRequest().toQuery()

                    val pageable = PageRequest.of(0, 10)
//                    val startTime = System.nanoTime()
                    val result = readAdapter.searchByCriteria(query, pageable)
//                    val endTime = System.nanoTime()
//                    val duration = (endTime - startTime) / 1_000_000 // Convert nanoseconds to milliseconds
//                    println("JPA Query Execution Time: ${duration}ms")

                    val content = result.content
                    content.forEach { product ->
//                        println(
//                            "Weight: ${product.likeCount * 10 + product.inquiryCount * 12 + product.optionLikeCount * 11}",
//                        )
                        if (query.availableSeasons.isNotEmpty()) {
                            product.availableSeasons.forEach { season -> season shouldBeIn query.availableSeasons }
                        }
                        if (query.cameraTypes.isNotEmpty()) {
                            product.cameraTypes.forEach { cameraType -> cameraType shouldBeIn query.cameraTypes }
                        }
                        if (query.retouchStyles.isNotEmpty()) {
                            product.retouchStyles.forEach { retouchStyle -> retouchStyle shouldBeIn query.retouchStyles }
                        }
                    }
                }
            }
        }
    })
