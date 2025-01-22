package kr.kro.dearmoment.product.application.port.out

import kr.kro.dearmoment.product.adapter.out.persistence.ProductEntity

interface ProductEntityRetrievalPort {
    fun getProductEntityById(id: Long): ProductEntity
}
