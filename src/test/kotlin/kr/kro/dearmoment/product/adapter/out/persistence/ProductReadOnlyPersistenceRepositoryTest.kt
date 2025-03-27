package kr.kro.dearmoment.product.adapter.out.persistence

import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderContext
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import jakarta.persistence.EntityManager
import kr.kro.dearmoment.RepositoryTest
import kr.kro.dearmoment.product.adapter.out.persistence.ProductPersistenceAdapterTest.Companion.createSampleProduct
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductType
import kr.kro.dearmoment.product.domain.model.ShootingPlace
import kr.kro.dearmoment.studio.adapter.output.persistence.StudioJpaRepository
import java.util.UUID

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
                    // dummy userId를 UUID로 선언
                    val dummyUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")
                    // 직접 StudioEntity 생성 (필요한 필드를 직접 지정)
                    val studio =
                        studioRepository.save(
                            // StudioEntity의 생성: id는 auto-generated
                            kr.kro.dearmoment.studio.adapter.output.persistence.StudioEntity(
                                id = 0L,
                                name = "스튜디오 디어모먼트",
                                userId = 123L,
                                contact = "010-1234-5678",
                                studioIntro = "소개글",
                                artistsIntro = "작가 소개",
                                instagramUrl = "instagram.com",
                                kakaoChannelUrl = "kakaotalk.com",
                                reservationNotice = "예약 안내",
                                cancellationPolicy = "취소 정책",
                                status = "ACTIVE",
                                partnerShops =
                                    mutableSetOf(
                                        kr.kro.dearmoment.studio.adapter.output.persistence.StudioPartnerShopEmbeddable(
                                            category = "HAIR_MAKEUP",
                                            name = "Test Shop",
                                            urlLink = "http://testshop.com",
                                        ),
                                    ),
                            ),
                        )
                    testProducts =
                        listOf(
                            createSampleProduct(
                                userId = dummyUserId,
                                title = "스냅 사진 기본 패키지",
                                productType = ProductType.WEDDING_SNAP,
                                shootingPlace = ShootingPlace.JEJU,
                            ),
                            createSampleProduct(
                                userId = dummyUserId,
                                title = "개인 스튜디오 대여",
                                productType = ProductType.WEDDING_SNAP,
                                shootingPlace = ShootingPlace.JEJU,
                            ),
                            createSampleProduct(
                                userId = dummyUserId,
                                title = "아기 사진 전문 촬영",
                                productType = ProductType.WEDDING_SNAP,
                                shootingPlace = ShootingPlace.JEJU,
                            ),
                        ).map { adapter.save(it, studio.id) }
                }

                it("제목으로 검색 시 해당 상품만 반환되어야 함") {
                    // testProducts에서 "스냅"이 포함된 제목만 필터링
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
