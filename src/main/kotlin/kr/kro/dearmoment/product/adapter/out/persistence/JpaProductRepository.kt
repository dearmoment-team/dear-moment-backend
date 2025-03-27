package kr.kro.dearmoment.product.adapter.out.persistence

import kr.kro.dearmoment.product.domain.model.ProductType
import kr.kro.dearmoment.product.domain.model.ShootingPlace
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface JpaProductRepository : JpaRepository<ProductEntity, Long> {
    @Modifying
    @Query("UPDATE ProductEntity p SET p.likeCount = p.likeCount + 1 WHERE p.productId = :productId")
    fun increaseLikeCount(
        @Param("productId") productId: Long,
    )

    @Modifying
    @Query("UPDATE ProductEntity p SET p.likeCount = p.likeCount - 1 WHERE p.productId = :productId AND p.likeCount > 0")
    fun decreaseLikeCount(
        @Param("productId") productId: Long,
    )

    @Modifying
    @Query("UPDATE ProductEntity p SET p.inquiryCount = p.inquiryCount + 1 WHERE p.productId = :productId")
    fun increaseInquiryCount(
        @Param("productId") productId: Long,
    )

    @Modifying
    @Query("UPDATE ProductEntity p SET p.inquiryCount = p.inquiryCount - 1 WHERE p.productId = :productId AND p.inquiryCount > 0")
    fun decreaseInquiryCount(
        @Param("productId") productId: Long,
    )

    /**
     * 특정 사용자의 Product 리스트를 조회합니다.
     * @param userId 사용자 ID
     * @return 해당 사용자의 Product 리스트
     */
    fun findByUserId(userId: UUID): List<ProductEntity>

    @Query(
        """
    SELECT p FROM ProductEntity p
    WHERE (:title IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%')))
  AND (:productType IS NULL OR p.productType = :productType)
  AND (:shootingPlace IS NULL OR p.shootingPlace = :shootingPlace)
    ORDER BY 
      CASE WHEN :sortBy = 'created-desc' THEN p.createdDate END DESC,
      CASE WHEN :sortBy = 'created-asc' THEN p.createdDate END ASC
    """,
    )
    fun searchByCriteria(
        @Param("title") title: String?,
        @Param("productType") productType: ProductType?,
        @Param("shootingPlace") shootingPlace: ShootingPlace?,
        @Param("sortBy") sortBy: String?,
    ): List<ProductEntity>

    /**
     * 특정 사용자 ID와 상품명 조합의 존재 여부를 확인합니다.
     * @param userId 사용자 ID
     * @param title 검색할 상품명
     * @return 존재 여부 (true/false)
     */
    fun existsByUserIdAndTitle(
        userId: UUID,
        title: String,
    ): Boolean
}
