package kr.kro.dearmoment.product.application.port.out

import kr.kro.dearmoment.product.domain.model.Product

interface ProductPersistencePort {
    /**
     * Product를 저장합니다.
     * @param product 저장할 도메인 모델
     * @return 저장된 Product
     */
    fun save(product: Product): Product

    /**
     * ID를 기준으로 Product를 조회합니다.
     * @param id 조회할 Product의 ID
     * @return 조회된 Product 또는 null
     */
    fun findById(id: Long): Product?

    /**
     * 모든 Product를 조회합니다.
     * @return 조회된 Product 리스트
     */
    fun findAll(): List<Product>

    /**
     * 특정 사용자의 Product를 조회합니다.
     * @param userId 사용자 ID
     * @return 해당 사용자의 Product 리스트
     */
    fun findByUserId(userId: Long): List<Product>

    /**
     * Product가 존재하는지 확인합니다.
     * @param id 확인할 Product의 ID
     * @return 존재 여부
     */
    fun existsById(id: Long): Boolean

    /**
     * 특정 조건에 따라 Product를 검색합니다.
     * @param title 검색할 Product의 제목 (nullable)
     * @param priceRange 검색할 가격 범위 (최소값, 최대값 nullable)
     * @return 조건에 맞는 Product 리스트
     */
    fun searchByCriteria(
        title: String?,
        priceRange: Pair<Long?, Long?>?,
        typeCode: Int?,
        sortBy: String?,
    ): List<Product>

    /**
     * ID를 기준으로 Product를 삭제합니다.
     * @param id 삭제할 Product의 ID
     */
    fun deleteById(id: Long)

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
