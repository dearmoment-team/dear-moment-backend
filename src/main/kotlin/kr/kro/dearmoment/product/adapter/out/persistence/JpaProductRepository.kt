package kr.kro.dearmoment.product.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface JpaProductRepository : JpaRepository<ProductEntity, Long> {
    /**
     * 특정 사용자의 Product 리스트를 조회합니다.
     * @param userId 사용자 ID
     * @return 해당 사용자의 Product 리스트
     */
    fun findByUserId(userId: Long): List<ProductEntity>

    @Query(
        """
    SELECT p FROM ProductEntity p 
    WHERE (:title IS NULL OR LOWER(p.title) LIKE LOWER('%'||:title||'%'))
    AND (:minPrice IS NULL OR p.price >= :minPrice)
    AND (:maxPrice IS NULL OR p.price <= :maxPrice)
    AND (:typeCode IS NULL OR p.typeCode = :typeCode)
    ORDER BY
        CASE 
            WHEN :sortBy = 'price-desc' THEN p.price 
        END DESC NULLS LAST,
        CASE 
            WHEN :sortBy = 'price-asc' THEN p.price 
        END ASC NULLS LAST
""",
    )
    fun searchByCriteria(
        @Param("title") title: String?,
        @Param("minPrice") minPrice: Long?,
        @Param("maxPrice") maxPrice: Long?,
        @Param("typeCode") typeCode: Int?,
        @Param("sortBy") sortBy: String?,
    ): List<ProductEntity>

    /**
     * 특정 사용자 ID와 상품명 조합의 존재 여부 확인
     * @param userId 사용자 ID
     * @param title 검색할 상품명
     * @return 존재 여부 (true/false)
     */
    fun existsByUserIdAndTitle(
        userId: Long,
        title: String,
    ): Boolean
}
