package kr.kro.dearmoment.product.application.dto.extensions

import kr.kro.dearmoment.product.application.dto.request.CreateProductOptionRequest
import kr.kro.dearmoment.product.application.dto.request.CreateProductRequest
import kr.kro.dearmoment.product.application.dto.request.UpdateProductOptionRequest
import kr.kro.dearmoment.product.application.dto.request.UpdateProductRequest
import kr.kro.dearmoment.product.application.dto.response.PartnerShopResponse
import kr.kro.dearmoment.product.application.dto.response.ProductOptionResponse
import kr.kro.dearmoment.product.application.dto.response.ProductResponse
import kr.kro.dearmoment.product.domain.model.PartnerShop
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.ProductOption

fun CreateProductRequest.toDomain(): Product {
    val partnerShopList =
        partnerShops.map { partnerShopRequest ->
            PartnerShop(
                name = partnerShopRequest.name,
                link = partnerShopRequest.link,
            )
        }

    return Product(
        userId = userId,
        title = title,
        description = description,
        price = price,
        typeCode = typeCode,
        shootingTime = shootingTime,
        shootingLocation = shootingLocation,
        numberOfCostumes = numberOfCostumes,
        partnerShops = partnerShopList,
        detailedInfo = detailedInfo,
        warrantyInfo = warrantyInfo,
        contactInfo = contactInfo,
        images = images,
        options = emptyList()
    )
}

fun CreateProductOptionRequest.toDomain(productId: Long): ProductOption {
    return ProductOption(
        productId = productId,
        name = this.name,
        additionalPrice = this.additionalPrice,
        description = this.description
    )
}

fun UpdateProductRequest.toDomain(): Product {
    val partnerShopList =
        partnerShops.map { partnerShopRequest ->
            PartnerShop(
                name = partnerShopRequest.name,
                link = partnerShopRequest.link,
            )
        }

    val productOptionList =
        options.map { optionRequest ->
            ProductOption(
                optionId = optionRequest.optionId,
                name = optionRequest.name,
                additionalPrice = optionRequest.additionalPrice,
                description = optionRequest.description,
            )
        }

    return Product(
        productId = productId,
        title = title,
        description = description,
        price = price,
        typeCode = typeCode,
        shootingTime = shootingTime,
        shootingLocation = shootingLocation,
        numberOfCostumes = numberOfCostumes,
        partnerShops = partnerShopList,
        detailedInfo = detailedInfo,
        warrantyInfo = warrantyInfo,
        contactInfo = contactInfo,
        options = productOptionList,
        images = images,
    )
}

fun UpdateProductOptionRequest.toDomain(): ProductOption {
    return ProductOption(
        optionId = this.optionId,
        name = this.name,
        additionalPrice = this.additionalPrice,
        description = this.description
    )
}

fun Product.toResponse() =
    ProductResponse(
        productId = productId!!,
        userId = userId!!,
        title = title,
        description = description,
        price = price,
        typeCode = typeCode,
        shootingTime = shootingTime,
        shootingLocation = shootingLocation,
        numberOfCostumes = numberOfCostumes,
        partnerShops = partnerShops.map { it.toResponse() },
        detailedInfo = detailedInfo,
        warrantyInfo = warrantyInfo,
        contactInfo = contactInfo,
        createdAt = createdAt,
        updatedAt = updatedAt,
        options = options.map { it.toResponse() },
        images = images,
    )

fun PartnerShop.toResponse() =
    PartnerShopResponse(
        name = name,
        link = link,
    )

fun ProductOption.toResponse() =
    ProductOptionResponse(
        optionId = optionId!!,
        productId = productId!!,
        name = name,
        additionalPrice = additionalPrice,
        description = description,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
