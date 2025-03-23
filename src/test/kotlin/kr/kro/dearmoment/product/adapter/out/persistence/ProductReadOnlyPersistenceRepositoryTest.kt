package kr.kro.dearmoment.product.adapter.out.persistence

import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderContext
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import jakarta.persistence.EntityManager
import kr.kro.dearmoment.RepositoryTest
import kr.kro.dearmoment.common.fixture.studioEntityFixture
import kr.kro.dearmoment.product.adapter.out.persistence.ProductPersistenceAdapterTest.Companion.createSampleProduct
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductType
import kr.kro.dearmoment.product.domain.model.ShootingPlace
import kr.kro.dearmoment.studio.adapter.output.persistence.StudioJpaRepository

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
    })
