package kr.kro.dearmoment.product.adapter.out.persistence

import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderContext
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import jakarta.persistence.EntityManager
import kr.kro.dearmoment.RepositoryTest
import kr.kro.dearmoment.common.fixture.productEntityFixture
import kr.kro.dearmoment.common.fixture.productOptionEntityFixture
import kr.kro.dearmoment.common.fixture.studioEntityFixture
import kr.kro.dearmoment.product.adapter.out.persistence.ProductPersistenceAdapterTest.Companion.createSampleProduct
import kr.kro.dearmoment.product.application.dto.query.SearchProductQuery
import kr.kro.dearmoment.product.domain.model.CameraType
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductType
import kr.kro.dearmoment.product.domain.model.RetouchStyle
import kr.kro.dearmoment.product.domain.model.ShootingPlace
import kr.kro.dearmoment.product.domain.model.ShootingSeason
import kr.kro.dearmoment.product.domain.model.option.PartnerShopCategory
import kr.kro.dearmoment.product.domain.sort.SortCriteria
import kr.kro.dearmoment.studio.adapter.output.persistence.StudioEntity
import kr.kro.dearmoment.studio.adapter.output.persistence.StudioJpaRepository
import org.springframework.data.domain.PageRequest

@RepositoryTest
class ProductReadOnlyPersistenceRepositoryTest(
    private val productRepository: JpaProductRepository,
    private val productOptionRepository: JpaProductOptionRepository,
    private val studioRepository: StudioJpaRepository,
    private val entityManager: EntityManager,
    private val jpqlRenderContext: JpqlRenderContext,
) : DescribeSpec({
        val adapter = ProductPersistenceAdapter(studioRepository, productRepository, productOptionRepository)
        val readAdapter = ProductReadOnlyRepository(productRepository, entityManager, jpqlRenderContext)

        afterTest {
            productOptionRepository.deleteAll()
            productRepository.deleteAll()
            studioRepository.deleteAll()
        }

        describe("ProductPersistenceAdapter 상품 검색 테스트") {
            context("상품 검색 기능") {
                lateinit var testProducts: List<Product>

                beforeEach {
                    val savedStudio = studioRepository.save(studioEntityFixture(userId = 1L))
                    testProducts =
                        listOf(
                            createSampleProduct(
                                userId = 1L,
                                title = "스냅 사진 기본 패키지",
                                productType = ProductType.WEDDING_SNAP,
                                shootingPlace = ShootingPlace.JEJU,
                            ),
                            createSampleProduct(
                                userId = 1L,
                                title = "개인 스튜디오 대여",
                                productType = ProductType.WEDDING_SNAP,
                                shootingPlace = ShootingPlace.JEJU,
                            ),
                            createSampleProduct(
                                userId = 1L,
                                title = "아기 사진 전문 촬영",
                                productType = ProductType.WEDDING_SNAP,
                                shootingPlace = ShootingPlace.JEJU,
                            ),
                        ).map { adapter.save(it, savedStudio.id) }
                }

                it("제목으로 검색 시 해당 상품만 반환되어야 함") {
                    // testProducts를 이용하여 기대값 생성
                    val expectedTitles = testProducts.filter { it.title.contains("스냅") }.map { it.title }
                    val results =
                        readAdapter.searchByCriteria(
                            title = "스냅",
                            productType = null,
                            shootingPlace = null,
                            sortBy = "created-desc",
                        )
                    results.map { it.title } shouldContainExactlyInAnyOrder expectedTitles
                }
            }
        }

        describe("searchByCriteria2()는") {

            // ✅ Given - 테스트 데이터 준비
            val studios = mutableListOf<StudioEntity>()
            var userId = 1L
            repeat(3) { studios.add(studioRepository.save(studioEntityFixture(userId = userId++))) }

            val products = mutableListOf<ProductEntity>()
            val options = mutableListOf<ProductOptionEntity>()
            val partnerShopCategories = mutableListOf<PartnerShopCategory>()
            val shootingSeasons = mutableListOf<ShootingSeason>()
            val retouchStyles = mutableListOf<RetouchStyle>()
            val cameraTypes = mutableListOf<CameraType>()
            studios.forEach { studio ->
                repeat(5) {
                    val product = productEntityFixture(userId = studio.userId, studioEntity = studio)
                    val savedProduct = productRepository.save(product)

                    product.retouchStyles.forEach { retouchStyles.add(it) }
                    product.availableSeasons.forEach { shootingSeasons.add(it) }
                    product.cameraTypes.forEach { cameraTypes.add(it) }
                    // 상품 옵션 추가 (가격 필터링을 위해)
                    val option = productOptionEntityFixture(productEntity = savedProduct)
                    option.partnerShops.forEach { partnerShopCategories.add(it.category!!) }

                    val savedOption = productOptionRepository.save(option)
                    options.add(savedOption)
                    products.add(savedProduct)
                }
            }

            context("검색 조건이 전달되면") {
                it("상품이 조회된다.") {
                    // ✅ When - 검색 조건 설정 후 실행
                    val query =
                        SearchProductQuery(
                            minPrice = 100_000L,
                            maxPrice = 200_000L,
                            partnerShopCategories = partnerShopCategories,
                            availableSeasons = shootingSeasons,
                            cameraTypes = cameraTypes,
                            retouchStyles = retouchStyles,
                            sortBy = SortCriteria.POPULAR,
                        )
                    val pageable = PageRequest.of(0, 10)

                    val result = readAdapter.searchByCriteria2(query, pageable)
                    val context = result.content
                }
            }
        }
    })
