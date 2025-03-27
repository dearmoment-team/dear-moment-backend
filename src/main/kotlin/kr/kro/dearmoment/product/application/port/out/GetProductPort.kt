package kr.kro.dearmoment.product.application.port.out

import kr.kro.dearmoment.product.application.dto.query.SearchProductQuery
import kr.kro.dearmoment.product.domain.model.Product
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface GetProductPort {
    /**
     * 특정 사용자 ID와 상품명 조합의 존재 여부를 확인합니다.
     * @param userId 사용자 ID
     * @param title 검색할 상품명
     * @return 존재 여부 (true/false)
     */
    fun existsByUserIdAndTitle(
        userId: Long,
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
    fun findByUserId(userId: Long): List<Product>

    /**
     * Product가 존재하는지 확인합니다.
     * @param id 확인할 Product의 ID
     * @return 존재 여부
     */
    fun existsById(id: Long): Boolean

    fun searchByCriteria(
        query: SearchProductQuery,
        pageable: Pageable,
    ): Page<Product>
}
