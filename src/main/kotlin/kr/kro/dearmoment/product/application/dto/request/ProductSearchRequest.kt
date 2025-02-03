package kr.kro.dearmoment.product.application.dto.request

data class ProductSearchRequest(
    val title: String? = null,
    val minPrice: Long? = null,
    val maxPrice: Long? = null,
    val typeCode: Int? = null,
    val sortBy: String? = null,
    val page: Int = 0,
    val size: Int = 10,
)
