package cz.lukynka.hollow

import io.realm.kotlin.MutableRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.query.RealmQuery
import io.realm.kotlin.types.TypedRealmObject
import kotlin.reflect.KClass

class CriteriaInfo(val string: String, val index: Int, val values: List<Any>)

sealed class Query {

    class QueryBuilder() {
        val infos = mutableListOf<Query>()

        fun equals(field: String, value: Any) {
            infos.add(Equals(field, value))
        }

        fun notEquals(field: String, value: Any) {
            infos.add(NotEquals(field, value))
        }

        fun greaterThan(field: String, value: Any) {
            infos.add(GreaterThan(field, value))
        }

        fun greaterThanOrEquals(field: String, value: Any) {
            infos.add(GreaterThanOrEqual(field, value))
        }

        fun lessThan(field: String, value: Any) {
            infos.add(LessThan(field, value))
        }

        fun lessThanOrEquals(field: String, value: Any) {
            infos.add(LessThanOrEquals(field, value))
        }

        fun isNull(field: String) {
            infos.add(IsNull(field))
        }

        fun contains(field: String, list: List<Any>) {
            infos.add(Contains(field, list))
        }

        fun notContains(field: String, list: List<Any>) {
            infos.add(NotContains(field, list))
        }

        fun build(): And {
            return And(*infos.toTypedArray())
        }
    }

    companion object {
        fun of(dls: QueryBuilder.() -> Unit): Query {
            val builder = QueryBuilder()
            dls.invoke(builder)
            return builder.build()
        }
    }

    class Or(vararg var criteria: Query) : Query() {
        fun add(c: Query) {
            val criteriaList = criteria.toMutableList()
            criteriaList.add(c)
            criteria = criteriaList.toTypedArray()
        }
    }

    class And(vararg var criteria: Query) : Query() {
        fun add(c: Query) {
            val criteriaList = criteria.toMutableList()
            criteriaList.add(c)
            criteria = criteriaList.toTypedArray()
        }
    }

    class Equals(val field: String, val value: Any) : Query()
    class NotEquals(val field: String, val value: Any) : Query()
    class GreaterThan(val field: String, val value: Any) : Query()
    class GreaterThanOrEqual(val field: String, val value: Any) : Query()
    class LessThan(val field: String, val value: Any) : Query()
    class LessThanOrEquals(val field: String, val value: Any) : Query()
    class IsNull(val field: String) : Query()
    class Contains(val field: String, val list: List<Any>) : Query()
    class NotContains(val field: String, val list: List<Any>) : Query()

    fun getCriteriaInfo(level: Int = 0, index: Int = 0): CriteriaInfo {
        var innerIndex = index
        return when (this) {
            is Equals -> CriteriaInfo("$field = $$index", index + 1, listOf(value))
            is NotEquals -> CriteriaInfo("$field != $$index", index + 1, listOf(value))
            is LessThan -> CriteriaInfo("$field < $$index", index + 1, listOf(value))
            is LessThanOrEquals -> CriteriaInfo("$field <= $$index", index + 1, listOf(value))
            is GreaterThan -> CriteriaInfo("$field > $$index", index + 1, listOf(value))
            is GreaterThanOrEqual -> CriteriaInfo("$field >= $$index", index + 1, listOf(value))
            is IsNull -> CriteriaInfo("$field IS NULL", index, emptyList())
            is Or -> {
                val values = mutableListOf<Any>()
                val criteriaString = criteria.joinToString(" OR ") {
                    val criteriaInfo = it.getCriteriaInfo(level + 1, innerIndex)
                    innerIndex = criteriaInfo.index
                    values.addAll(criteriaInfo.values)
                    criteriaInfo.string
                }
                CriteriaInfo(if (level == 0) criteriaString else "($criteriaString)", innerIndex, values)
            }

            is And -> {
                val values = mutableListOf<Any>()
                val criteriaString = criteria.joinToString(" AND ") {
                    val criteriaInfo = it.getCriteriaInfo(level + 1, innerIndex)
                    innerIndex = criteriaInfo.index
                    values.addAll(criteriaInfo.values)
                    criteriaInfo.string
                }
                CriteriaInfo(if (level == 0) criteriaString else "($criteriaString)", innerIndex, values)
            }

            is Contains -> {
                if (list.isNotEmpty()) {
                    val criteriaString = list.joinToString(" OR ") {
                        "$field = $${innerIndex++}"
                    }
                    CriteriaInfo(if (list.count() > 1) "($criteriaString)" else criteriaString, innerIndex, list)
                } else {
                    CriteriaInfo("", innerIndex, emptyList())
                }
            }

            is NotContains -> {
                if (list.isNotEmpty()) {
                    val criteriaString = list.joinToString(" OR ") {
                        "$field = $${innerIndex++}"
                    }
                    CriteriaInfo(if (list.count() > 1) "NOT ($criteriaString)" else "NOT $criteriaString", innerIndex, list)
                } else {
                    CriteriaInfo("", innerIndex, emptyList())
                }
            }
        }
    }
}

fun <T : TypedRealmObject> Realm.criteriaQuery(clazz: KClass<T>, query: Query): RealmQuery<T> {
    val criteriaInfo = query.getCriteriaInfo()
    return if (criteriaInfo.string == "") {
        query(clazz)
    } else {
        query(clazz, criteriaInfo.string, *criteriaInfo.values.toTypedArray())
    }
}

fun <T : TypedRealmObject> MutableRealm.criteriaQuery(clazz: KClass<T>, query: Query): RealmQuery<T> {
    val criteriaInfo = query.getCriteriaInfo()
    return if (criteriaInfo.string == "") {
        query(clazz)
    } else {
        query(clazz, criteriaInfo.string, *criteriaInfo.values.toTypedArray())
    }
}