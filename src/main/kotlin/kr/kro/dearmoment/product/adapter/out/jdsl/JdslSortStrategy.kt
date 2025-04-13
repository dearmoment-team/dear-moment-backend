package kr.kro.dearmoment.product.adapter.out.jdsl

import com.linecorp.kotlinjdsl.querymodel.jpql.expression.Expressions.max
import com.linecorp.kotlinjdsl.querymodel.jpql.expression.Expressions.min
import com.linecorp.kotlinjdsl.querymodel.jpql.expression.Expressions.plus
import com.linecorp.kotlinjdsl.querymodel.jpql.expression.Expressions.times
import com.linecorp.kotlinjdsl.querymodel.jpql.expression.Expressions.value
import com.linecorp.kotlinjdsl.querymodel.jpql.path.Paths.path
import com.linecorp.kotlinjdsl.querymodel.jpql.sort.Sort
import com.linecorp.kotlinjdsl.querymodel.jpql.sort.Sorts
import kr.kro.dearmoment.like.domain.SortCriteria
import kr.kro.dearmoment.product.adapter.out.persistence.ProductEntity
import kr.kro.dearmoment.product.adapter.out.persistence.ProductOptionEntity
import kr.kro.dearmoment.studio.adapter.output.persistence.StudioEntity

object JdslSortStrategy {
    fun SortCriteria.toJdslComparator(): Sort {
        return when (this) {
            SortCriteria.RECOMMENDED -> {
                Sorts.desc(
                    plus(
                        plus(
                            times(path(ProductEntity::likeCount), value(10L)),
                            times(path(ProductEntity::inquiryCount), value(12L)),
                        ),
                        plus(
                            times(path(ProductEntity::optionLikeCount), value(11L)),
                            times(path(StudioEntity::cast), value(11)),
                        ),
                    ),
                )
            }
            SortCriteria.POPULAR -> {
                Sorts.desc(
                    plus(
                        plus(
                            times(path(ProductEntity::likeCount), value(10L)),
                            times(path(ProductEntity::inquiryCount), value(12L)),
                        ),
                        times(path(ProductEntity::optionLikeCount), value(11L)),
                    ),
                )
            }
            SortCriteria.PRICE_LOW -> {
                Sorts.asc(min(false, path(ProductOptionEntity::discountPrice)))
            }
            SortCriteria.PRICE_HIGH -> {
                (Sorts.desc(max(false, path(ProductOptionEntity::discountPrice))))
            }
        }
    }
}
