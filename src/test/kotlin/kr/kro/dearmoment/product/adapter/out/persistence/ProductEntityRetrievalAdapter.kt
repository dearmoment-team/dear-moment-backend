package kr.kro.dearmoment.product.adapter.out.persistence

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * ProductEntityRetrievalAdapterTest는 ProductEntityRetrievalAdapter 클래스의 동작을 검증하는 테스트 클래스입니다.
 */
@DataJpaTest
@Transactional
@Import(ProductEntityRetrievalAdapter::class)
class ProductEntityRetrievalAdapterTest(
    private val productEntityRetrievalAdapter: ProductEntityRetrievalAdapter,
    private val jpaProductRepository: JpaProductRepository, // Repository 주입
) : StringSpec({

    "특정 ID로 엔티티를 조회한다" {
        // given
        val productEntity = ProductEntity(
            userId = 1L,
            title = "엔티티 조회 테스트",
            description = "엔티티 설명",
            price = 15000L,
            typeCode = 2,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            options = mutableListOf()
        )
        val savedEntity = jpaProductRepository.saveAndFlush(productEntity)

        // when
        val retrievedEntity = productEntityRetrievalAdapter.getEntityById(savedEntity.productId!!)

        // then
        retrievedEntity.productId shouldBe savedEntity.productId
        retrievedEntity.title shouldBe "엔티티 조회 테스트"
        retrievedEntity.price shouldBe 15000L
    }

    "존재하지 않는 ID로 조회 시 예외가 발생한다" {
        // given
        val nonExistentId = 999L

        // when & then
        val exception = shouldThrow<IllegalArgumentException> {
            productEntityRetrievalAdapter.getEntityById(nonExistentId)
        }
        exception.message shouldBe "Product with ID $nonExistentId not found"
    }

    "엔티티 저장 후 필드값을 확인한다" {
        // given
        val productEntity = ProductEntity(
            userId = 2L,
            title = "필드 확인 테스트",
            description = "필드 확인",
            price = 20000L,
            typeCode = 3,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val savedEntity = jpaProductRepository.saveAndFlush(productEntity)

        // 옵션 생성 및 저장
        val optionEntity = ProductOptionEntity(
            name = "옵션 테스트",
            additionalPrice = 1000L,
            description = "옵션 설명",
            product = savedEntity,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        savedEntity.options.add(optionEntity)
        jpaProductRepository.saveAndFlush(savedEntity)

        // when
        val retrievedEntity = productEntityRetrievalAdapter.getEntityById(savedEntity.productId!!)

        // then
        retrievedEntity.productId shouldBe savedEntity.productId
        retrievedEntity.title shouldBe "필드 확인 테스트"
        retrievedEntity.options.first().name shouldBe "옵션 테스트"
    }
})
