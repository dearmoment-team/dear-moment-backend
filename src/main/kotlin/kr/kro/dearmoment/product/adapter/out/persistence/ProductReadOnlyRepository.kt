package kr.kro.dearmoment.product.adapter.out.persistence

import com.linecorp.kotlinjdsl.render.RenderContext
import jakarta.persistence.EntityManager
import kr.kro.dearmoment.product.application.port.out.GetProductPort
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductType
import kr.kro.dearmoment.product.domain.model.ShootingPlace
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class ProductReadOnlyRepository(
    private val productJpaRepository: JpaProductRepository,
    private val entityManager: EntityManager,
    private val jpqlRenderContext: RenderContext,
) : GetProductPort {
    override fun findById(id: Long): Product? {
        return productJpaRepository.findByIdOrNull(id)?.toDomain()
    }

    override fun findAll(): List<Product> {
        return productJpaRepository.findAll().map { it.toDomain() }
    }

    override fun findByUserId(userId: UUID): List<Product> {
        return productJpaRepository.findByUserId(userId).map { it.toDomain() }
    }

    override fun existsById(id: Long): Boolean {
        return productJpaRepository.existsById(id)
    }

    override fun searchByCriteria(
        title: String?,
        productType: String?,
        shootingPlace: String?,
        sortBy: String?,
    ): List<Product> {
        val pt: ProductType? = productType?.let { ProductType.valueOf(it) }
        val sp: ShootingPlace? = shootingPlace?.let { ShootingPlace.valueOf(it) }
        val productEntities = productJpaRepository.searchByCriteria(title, pt, sp, sortBy)
        return productEntities.map { it.toDomain() }
    }

    override fun existsByUserIdAndTitle(
        userId: UUID,
        title: String,
    ): Boolean = productJpaRepository.existsByUserIdAndTitle(userId, title)
}
