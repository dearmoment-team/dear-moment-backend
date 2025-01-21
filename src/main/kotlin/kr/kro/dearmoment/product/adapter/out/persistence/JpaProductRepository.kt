package kr.kro.dearmoment.product.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface JpaProductRepository : JpaRepository<ProductEntity, Long>
