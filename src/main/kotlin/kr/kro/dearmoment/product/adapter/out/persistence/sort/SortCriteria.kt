package kr.kro.dearmoment.product.adapter.out.persistence.sort

import com.linecorp.kotlinjdsl.querymodel.jpql.expression.Expressions.max
import com.linecorp.kotlinjdsl.querymodel.jpql.expression.Expressions.min
import com.linecorp.kotlinjdsl.querymodel.jpql.expression.Expressions.plus
import com.linecorp.kotlinjdsl.querymodel.jpql.expression.Expressions.times
import com.linecorp.kotlinjdsl.querymodel.jpql.expression.Expressions.value
import com.linecorp.kotlinjdsl.querymodel.jpql.path.Paths.path
import com.linecorp.kotlinjdsl.querymodel.jpql.sort.Sort
import com.linecorp.kotlinjdsl.querymodel.jpql.sort.Sorts
import kr.kro.dearmoment.product.adapter.out.persistence.ProductEntity
import kr.kro.dearmoment.product.adapter.out.persistence.ProductOptionEntity
import kr.kro.dearmoment.studio.adapter.output.persistence.StudioEntity

enum class SortCriteria(
    val strategy: Sort,
) {
    RECOMMENDED(
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
        ),
    ),
    POPULAR(
        Sorts.desc(
            plus(
                plus(
                    times(path(ProductEntity::likeCount), value(10L)),
                    times(path(ProductEntity::inquiryCount), value(12L)),
                ),
                times(path(ProductEntity::optionLikeCount), value(11L)),
            ),
        ),
    ),
    PRICE_LOW(Sorts.asc(min(false, path(ProductOptionEntity::discountPrice)))),
    PRICE_HIGH(Sorts.desc(max(false, path(ProductOptionEntity::discountPrice)))),
    ;

    companion object {
        fun from(value: String): SortCriteria =
            SortCriteria.entries.find { it.name == value }
                ?: throw IllegalArgumentException("유효하지 않은 ProductSortCriteria 값: $value")
    }
}
