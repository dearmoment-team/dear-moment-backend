package kr.kro.dearmoment.product.adapter.out.persistence

import com.linecorp.kotlinjdsl.render.RenderContext
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import io.kotest.matchers.shouldBe
import jakarta.persistence.EntityManager
import kr.kro.dearmoment.RepositoryTest
import kr.kro.dearmoment.common.fixture.productEntityFixture
import kr.kro.dearmoment.common.fixture.productOptionEntityFixture
import kr.kro.dearmoment.common.fixture.studioEntityFixture
import kr.kro.dearmoment.like.domain.SortCriteria
import kr.kro.dearmoment.product.application.dto.query.SearchProductQuery
import kr.kro.dearmoment.product.application.dto.request.SearchProductRequest
import kr.kro.dearmoment.product.domain.model.CameraType
import kr.kro.dearmoment.product.domain.model.RetouchStyle
import kr.kro.dearmoment.product.domain.model.ShootingSeason
import kr.kro.dearmoment.product.domain.model.option.PartnerShopCategory
import kr.kro.dearmoment.studio.adapter.output.persistence.StudioEntity
import kr.kro.dearmoment.studio.adapter.output.persistence.StudioJpaRepository
import org.springframework.data.domain.PageRequest
import java.util.UUID

@RepositoryTest
class ProductReadOnlyPersistenceRepositoryTest(
    private val productRepository: JpaProductRepository,
    private val productOptionRepository: JpaProductOptionRepository,
    private val studioRepository: StudioJpaRepository,
    private val entityManager: EntityManager,
    private val jpqlRenderContext: RenderContext,
) : DescribeSpec({

        val readAdapter = ProductReadOnlyRepository(productRepository, entityManager, jpqlRenderContext)

        afterTest {
            productOptionRepository.deleteAll()
            productRepository.deleteAll()
            studioRepository.deleteAll()
        }

        describe("findWithStudioById()는") {
            val studio = studioRepository.save(studioEntityFixture(userId = UUID.randomUUID()))
            val product = productRepository.save(productEntityFixture(userId = studio.userId, studioEntity = studio))

            context("상품 id가 전달되면") {
                it("해당 상품과 스튜디오를 DB에서 조회한다.") {
                    val result = readAdapter.findWithStudioById(product.productId!!)
                    result.productId shouldBe product.productId!!
                    result.studio!!.id shouldBe studio.id
                }
            }
        }

        describe("searchByCriteria()는") {
            // ── 테스트 데이터 셋업 ──────────────────────────────
            val studios = mutableListOf<StudioEntity>()
            repeat(5) { studios += studioRepository.save(studioEntityFixture(userId = UUID.randomUUID())) }

            val partnerShopCategories = mutableSetOf<PartnerShopCategory>()
            val shootingSeasons = mutableSetOf<ShootingSeason>()
            val retouchStyles = mutableSetOf<RetouchStyle>()
            val cameraTypes = mutableSetOf<CameraType>()

            studios.forEach { studio ->
                repeat(2) {
                    val product = productRepository.save(productEntityFixture(userId = studio.userId, studioEntity = studio))

                    product.retouchStyles.forEach { retouchStyles += it }
                    product.availableSeasons.forEach { shootingSeasons += it }
                    product.cameraTypes.forEach { cameraTypes += it }

                    // 옵션 2개 생성
                    listOf(
                        productOptionEntityFixture(ProductEntity.fromDomain(product.toDomain(), studio)),
                        productOptionEntityFixture(ProductEntity.fromDomain(product.toDomain(), studio))
                    ).forEach { option ->
                        option.partnerShops.forEach { partnerShopCategories += it.category!! }
                        product.options += option
                        productOptionRepository.save(option)
                    }
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

                    val result = readAdapter.searchByCriteria(query, PageRequest.of(0, 10))

                    result.forEach { product ->
                        product.options.forEach { option ->
                            option.discountPrice shouldBeGreaterThanOrEqualTo query.minPrice
                            option.discountPrice shouldBeLessThanOrEqualTo query.maxPrice
                            option.partnerShops.forEach { it.category shouldBeIn query.partnerShopCategories }
                        }
                        product.availableSeasons.forEach { it shouldBeIn query.availableSeasons }
                        product.cameraTypes.forEach { it shouldBeIn query.cameraTypes }
                        product.retouchStyles.forEach { it shouldBeIn query.retouchStyles }
                    }
                }
            }

            context("검색 조건이 비어 있어도") {
                it("상품이 조회된다.") {
                    val query = SearchProductRequest().toQuery()
                    val result = readAdapter.searchByCriteria(query, PageRequest.of(0, 10))

                    result.forEach { product ->
                        if (query.availableSeasons.isNotEmpty()) {
                            product.availableSeasons.forEach { it shouldBeIn query.availableSeasons }
                        }
                        if (query.cameraTypes.isNotEmpty()) {
                            product.cameraTypes.forEach { it shouldBeIn query.cameraTypes }
                        }
                        if (query.retouchStyles.isNotEmpty()) {
                            product.retouchStyles.forEach { it shouldBeIn query.retouchStyles }
                        }
                    }
                }
            }

            context("검색 조건이 가격순이면") {
                it("상품이 조회된다.") {
                    val query =
                        SearchProductQuery(
                            minPrice = 50_000L,
                            maxPrice = 200_000L,
                            partnerShopCategories = emptyList(),
                            availableSeasons = emptyList(),
                            cameraTypes = emptyList(),
                            retouchStyles = emptyList(),
                            sortBy = SortCriteria.PRICE_HIGH,
                        )

                    val result = readAdapter.searchByCriteria(query, PageRequest.of(0, 10))

                    result.forEach { product ->
                        product.options.forEach {
                            it.discountPrice shouldBeGreaterThanOrEqualTo query.minPrice
                            it.discountPrice shouldBeLessThanOrEqualTo query.maxPrice
                        }
                    }
                }
            }
        }
    })
