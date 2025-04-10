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
import kr.kro.dearmoment.product.adapter.out.persistence.sort.SortCriteria.POPULAR
import kr.kro.dearmoment.product.adapter.out.persistence.sort.SortCriteria.PRICE_HIGH
import kr.kro.dearmoment.product.adapter.out.persistence.sort.SortCriteria.PRICE_LOW
import kr.kro.dearmoment.product.adapter.out.persistence.sort.SortCriteria.RECOMMENDED
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
                like.product.options
                    .find { option ->
                        option.discountPrice in query.minPrice..query.maxPrice &&
                            matchesIfNotEmpty(
                                option.partnerShops.map { it.category }.toSet(),
                                query.partnerShopCategories
                            )
                    } != null
            }.filter {
                matchesIfNotEmpty(it.product.availableSeasons, query.availableSeasons) &&
                    matchesIfNotEmpty(it.product.cameraTypes, query.cameraTypes) &&
                    matchesIfNotEmpty(it.product.retouchStyles, query.retouchStyles)
            }.sortedWith(
                when (query.sortBy) {
                    PRICE_LOW ->
                        compareBy {
                            it.product.options.minOf { it.discountPrice }
                        }

                    PRICE_HIGH ->
                        compareByDescending {
                            it.product.options.maxOf { it.discountPrice }
                        }

                    POPULAR ->
                        compareByDescending {
                            it.product.likeCount * 10L +
                                it.product.inquiryCount * 12L +
                                it.product.optionLikeCount * 11L
                        }

                    RECOMMENDED ->
                        compareByDescending {
                            it.product.likeCount * 10L +
                                it.product.inquiryCount * 12L +
                                it.product.optionLikeCount * 11L +
                                if (it.product.studio?.isCasted == true) 11L else 0L
                        }
                }
            )

        return results.map { GetProductLikeResponse.from(it) }
    }

    override fun filterUserProductsOptionsLikes(
        userId: UUID,
        query: FilterUserLikesQuery,
    ): List<GetProductOptionLikeResponse> {
        val userLikes = getLikePort.findUserProductOptionLikes(userId)
        val results =
            userLikes.filter { like ->
                like.product.options
                    .find { option ->
                        option.optionId == like.productOptionId &&
                            option.discountPrice in query.minPrice..query.maxPrice &&
                            matchesIfNotEmpty(
                                option.partnerShops.map { it.category }.toSet(),
                                query.partnerShopCategories
                            )
                    } != null
            }.filter {
                matchesIfNotEmpty(it.product.availableSeasons, query.availableSeasons) &&
                    matchesIfNotEmpty(it.product.cameraTypes, query.cameraTypes) &&
                    matchesIfNotEmpty(it.product.retouchStyles, query.retouchStyles)
            }.sortedWith(
                when (query.sortBy) {
                    PRICE_LOW ->
                        compareBy {
                            it.product.options.find { option -> option.optionId == it.productOptionId }?.discountPrice
                        }

                    PRICE_HIGH ->
                        compareByDescending {
                            it.product.options.find { option -> option.optionId == it.productOptionId }?.discountPrice
                        }

                    POPULAR ->
                        compareByDescending {
                            it.product.likeCount * 10L +
                                it.product.inquiryCount * 12L +
                                it.product.optionLikeCount * 11L
                        }

                    RECOMMENDED ->
                        compareByDescending {
                            it.product.likeCount * 10L +
                                it.product.inquiryCount * 12L +
                                it.product.optionLikeCount * 11L +
                                if (it.product.studio?.isCasted == true) 11L else 0L
                        }
                }
            )

        return results.map { GetProductOptionLikeResponse.from(it) }
    }

    override fun isProductLike(query: ExistLikeQuery): Boolean = getLikePort.existProductLike(query.userId, query.targetId)

    override fun isProductOptionLike(query: ExistLikeQuery): Boolean = getLikePort.existProductOptionLike(query.userId, query.targetId)

    private fun <T> matchesIfNotEmpty(
        target: Set<T>,
        query: Set<T>
    ): Boolean {
        return query.isEmpty() || target.intersect(query).isNotEmpty()
    }
}
