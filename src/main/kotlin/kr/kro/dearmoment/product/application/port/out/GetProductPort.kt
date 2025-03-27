package kr.kro.dearmoment.product.application.port.out

import kr.kro.dearmoment.product.domain.model.Product
import java.util.*

interface GetProductPort {
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
    fun findByUserId(userId: UUID): List<Product>

    /**
     * Product가 존재하는지 확인합니다.
     * @param id 확인할 Product의 ID
     * @return 존재 여부
     */
    fun existsById(id: Long): Boolean

    /**
     * 특정 조건에 따라 Product를 검색합니다.
     *
     * 기존에는 basePrice를 기준으로 검색하였으나, 도메인 변경에 따라
     * 이제는 상품의 제목, 상품 유형(productType), 촬영 장소(shootingPlace) 등을
     * 조건으로 검색합니다.
     *
     * @param title 검색할 Product의 제목 (nullable)
     * @param productType 검색할 상품 유형 (nullable, 예: "WEDDING_SNAP")
     * @param shootingPlace 검색할 촬영 장소 (nullable, 예: "JEJU")
     * @param sortBy 정렬 조건 (nullable, 예: "created-desc", "created-asc")
     * @return 조건에 맞는 Product 리스트
     */
    fun searchByCriteria(
        title: String?,
        productType: String?,
        shootingPlace: String?,
        sortBy: String?,
    ): List<Product>
}
