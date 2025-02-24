// package kr.kro.dearmoment.product.application.dto.request
//
// import kr.kro.dearmoment.product.domain.model.Product
// import kr.kro.dearmoment.product.domain.model.ProductOption
// import java.time.LocalDateTime
//
// data class UpdateProductRequest(
//    val productId: Long,
//    val userId: Long,
//    val title: String,
//    val description: String?,
//    val price: Long,
//    val typeCode: Int,
//    val shootingTime: LocalDateTime?,
//    val shootingLocation: String?,
//    val numberOfCostumes: Int?,
//    val partnerShops: List<UpdatePartnerShopRequest>,
//    val detailedInfo: String?,
//    val warrantyInfo: String?,
//    val contactInfo: String?,
//    val options: List<UpdateProductOptionRequest>,
//    val images: List<String>,
// ) {
//    companion object {
//        fun toDomain(request: UpdateProductRequest): Product {
//            val partnerShopList =
//                request.partnerShops.map { partnerShopRequest ->
//                    kr.kro.dearmoment.product.domain.model.PartnerShop(
//                        name = partnerShopRequest.name,
//                        link = partnerShopRequest.link,
//                    )
//                }
//            val productOptionList =
//                request.options.map { optionRequest ->
//                    // 옵션 변환 시, productId를 UpdateProductRequest의 productId로 전달
//                    UpdateProductOptionRequest.toDomain(optionRequest, request.productId)
//                }
//            return Product(
//                productId = request.productId,
//                userId = request.userId,
//                title = request.title,
//                description = request.description ?: "",
//                price = request.price,
//                typeCode = request.typeCode,
//                shootingTime = request.shootingTime,
//                shootingLocation = request.shootingLocation ?: "",
//                numberOfCostumes = request.numberOfCostumes ?: 0,
//                partnerShops = partnerShopList,
//                detailedInfo = request.detailedInfo ?: "",
//                warrantyInfo = request.warrantyInfo ?: "",
//                contactInfo = request.contactInfo ?: "",
//                options = productOptionList,
//                images = request.images,
//            )
//        }
//    }
// }
//
// data class UpdatePartnerShopRequest(
//    val name: String,
//    val link: String,
// )
//
// data class UpdateProductOptionRequest(
//    val optionId: Long?,
//    val name: String,
//    val additionalPrice: Long,
//    val description: String?,
// ) {
//    companion object {
//        fun toDomain(
//            request: UpdateProductOptionRequest,
//            productId: Long,
//        ): ProductOption {
//            return ProductOption(
//                optionId = request.optionId ?: 0L,
//                productId = productId,
//                name = request.name,
//                additionalPrice = request.additionalPrice,
//                description = request.description ?: "",
//            )
//        }
//    }
// }
