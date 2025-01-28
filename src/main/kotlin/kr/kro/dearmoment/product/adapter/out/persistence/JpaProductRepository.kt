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

    /**
     * 조건에 따라 Product 리스트를 검색합니다.
     * @param title 검색할 Product의 제목 (nullable)
     * @param minPrice 최소 가격 (nullable)
     * @param maxPrice 최대 가격 (nullable)
     * @return 조건에 맞는 Product 리스트
     */
    @Query("""
        SELECT p FROM ProductEntity p 
        WHERE (:title IS NULL OR p.title LIKE %:title%) 
        AND (:minPrice IS NULL OR p.price >= :minPrice) 
        AND (:maxPrice IS NULL OR p.price <= :maxPrice)
    """)
    fun searchByCriteria(
        @Param("title") title: String?,
        @Param("minPrice") minPrice: Long?,
        @Param("maxPrice") maxPrice: Long?
    ): List<ProductEntity>

    /**
     * 특정 사용자 ID와 상품명 조합의 존재 여부 확인
     * @param userId 사용자 ID
     * @param title 검색할 상품명
     * @return 존재 여부 (true/false)
     */
    fun existsByUserIdAndTitle(userId: Long, title: String): Boolean
}
