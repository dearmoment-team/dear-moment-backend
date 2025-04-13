package kr.kro.dearmoment.like.application.service

import kr.kro.dearmoment.common.dto.PagedResponse
import kr.kro.dearmoment.like.application.dto.GetProductLikeResponse
import kr.kro.dearmoment.like.application.dto.GetProductOptionLikeResponse
import kr.kro.dearmoment.like.application.port.input.LikeQueryUseCase
import kr.kro.dearmoment.like.application.port.output.GetLikePort
import kr.kro.dearmoment.like.application.query.ExistLikeQuery
import kr.kro.dearmoment.like.application.query.FilterUserLikesQuery
import kr.kro.dearmoment.like.application.query.GetUserProductLikeQuery
import kr.kro.dearmoment.like.application.query.GetUserProductOptionLikeQuery
import kr.kro.dearmoment.product.domain.model.CameraType
import kr.kro.dearmoment.product.domain.model.Product
import kr.kro.dearmoment.product.domain.model.RetouchStyle
import kr.kro.dearmoment.product.domain.model.ShootingSeason
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class LikeQueryService(
    private val getLikePort: GetLikePort,
) : LikeQueryUseCase {
    override fun getUserProductLikes(query: GetUserProductLikeQuery): PagedResponse<GetProductLikeResponse> {
        val userLikes = getLikePort.findUserProductLikes(query.userId, query.pageable)
        return PagedResponse(
            content = userLikes.content.map { GetProductLikeResponse.from(it) },
            page = query.pageable.pageNumber,
            size = query.pageable.pageSize,
        )
    }

    override fun getUserProductOptionLikes(query: GetUserProductOptionLikeQuery): PagedResponse<GetProductOptionLikeResponse> {
        val userLikes = getLikePort.findUserProductOptionLikes(query.userId, query.pageable)
        return PagedResponse(
            content = userLikes.content.map { GetProductOptionLikeResponse.from(it) },
            page = query.pageable.pageNumber,
            size = query.pageable.pageSize,
        )
    }

    override fun filterUserProductsLikes(
        userId: UUID,
        query: FilterUserLikesQuery,
    ): List<GetProductLikeResponse> {
        val userLikes = getLikePort.findUserProductLikes(userId)
        val results =
            userLikes.filter { like ->
                like.product.options.any { option ->
                    option.isPriceInRange(query.minPrice, query.maxPrice) &&
                        option.partnerShops.map { it.category }.matchesAny(query.partnerShopCategories)
                }
            }.filter {
                it.product.matches(query.availableSeasons, query.cameraTypes, query.retouchStyles)
            }.sortedWith(query.sortBy.toProductLikeComparator())

        return results.map { GetProductLikeResponse.from(it) }
    }

    override fun filterUserProductsOptionsLikes(
        userId: UUID,
        query: FilterUserLikesQuery,
    ): List<GetProductOptionLikeResponse> {
        val userLikes = getLikePort.findUserProductOptionLikes(userId)
        val results =
            userLikes.filter { like ->
                like.product.options.any { option ->
                    like.productOptionId == option.optionId &&
                        option.isPriceInRange(query.minPrice, query.maxPrice) &&
                        option.partnerShops.map { it.category }.matchesAny(query.partnerShopCategories)
                }
            }.filter {
                it.product.matches(query.availableSeasons, query.cameraTypes, query.retouchStyles)
            }.sortedWith(query.sortBy.toProductOptionLikeComparator())

        return results.map { GetProductOptionLikeResponse.from(it) }
    }

    override fun isProductLike(query: ExistLikeQuery): Boolean = getLikePort.existProductLike(query.userId, query.targetId)

    override fun isProductOptionLike(query: ExistLikeQuery): Boolean = getLikePort.existProductOptionLike(query.userId, query.targetId)

    private fun Product.matches(
        availableSeasons: Set<ShootingSeason>,
        cameraTypes: Set<CameraType>,
        retouchStyles: Set<RetouchStyle>,
    ): Boolean =
        this.availableSeasons.matchesAny(availableSeasons) &&
            this.cameraTypes.matchesAny(cameraTypes) &&
            this.retouchStyles.matchesAny(retouchStyles)

    private fun <T> Collection<T>.matchesAny(other: Collection<T>): Boolean = other.isEmpty() || other.any { it in this }
}
