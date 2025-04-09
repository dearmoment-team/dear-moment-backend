package kr.kro.dearmoment.product.adapter.out.persistence.jdsl

import com.linecorp.kotlinjdsl.dsl.jpql.Jpql
import com.linecorp.kotlinjdsl.dsl.jpql.join.AssociationJoinOnStep
import com.linecorp.kotlinjdsl.querymodel.jpql.expression.Expressionable
import com.linecorp.kotlinjdsl.querymodel.jpql.expression.Expressions
import com.linecorp.kotlinjdsl.querymodel.jpql.predicate.Predicate
import com.linecorp.kotlinjdsl.querymodel.jpql.predicate.Predicates
import kotlin.reflect.KProperty1

inline fun <T : Any, reified V, S : Collection<V>> Jpql.joinIfNotEmpty(
    collection: S,
    property: () -> KProperty1<T, S>
): AssociationJoinOnStep<V & Any>? {
    return if (collection.isNotEmpty()) {
        fetchJoin(property())
    } else {
        null
    }
}

fun <T : Any, S : T?> Expressionable<T>.inIfNotEmpty(compareValues: Iterable<S>): Predicate? {
    return if (compareValues.any()) Predicates.`in`(this.toExpression(), compareValues.map { Expressions.value(it) }) else null
}
