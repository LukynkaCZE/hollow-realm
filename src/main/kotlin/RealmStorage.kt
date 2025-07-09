package cz.lukynka.hollow

import cz.lukynka.hollow.Query.QueryBuilder
import io.realm.kotlin.Realm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.types.RealmObject
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KClass

abstract class RealmStorage<T : RealmObject>(val kclass: KClass<T>) {

    private val realm: Realm get() = HollowRealm.realm

    fun compact(): Boolean {
        return Realm.compactRealm(HollowRealm.realm.configuration)
    }

    fun delete(value: T) {
        realm.writeBlocking {
            val liveObj = this.findLatest(value) ?: return@writeBlocking
            this.delete(liveObj)
        }
    }

    fun deleteAsync(value: T): CompletableFuture<Unit> {
        return CompletableFuture.supplyAsync {
            this.delete(value)
        }
    }

    fun write(value: T) {
        realm.writeBlocking {
            this.copyToRealm(value, updatePolicy = UpdatePolicy.ALL)
        }
    }

    fun writeAsync(value: T): CompletableFuture<Unit> {
        return CompletableFuture.supplyAsync {
            this.write(value)
        }
    }

    fun compactAsync(): CompletableFuture<Boolean> {
        return CompletableFuture.supplyAsync {
            this.compact()
        }
    }

    fun all(): List<T> {
        return realm.copyFromRealm(realm.query(kclass).find())
    }

    fun allAsync(): CompletableFuture<List<T>> {
        return CompletableFuture.supplyAsync {
            this.all()
        }
    }

    fun query(query: Query): List<T> {
        val crit = realm.criteriaQuery(kclass, query)
        return realm.copyFromRealm(crit.find())
    }

    fun query(dls: QueryBuilder.() -> Unit): List<T> {
        val builder = QueryBuilder()
        dls.invoke(builder)
        val query = builder.build()

        return query(query)
    }

    fun queryAsync(query: Query): CompletableFuture<List<T>> {
        return CompletableFuture.supplyAsync { this.query(query) }
    }

    fun queryAsync(dls: QueryBuilder.() -> Unit): CompletableFuture<List<T>> {
        return CompletableFuture.supplyAsync {
            val builder = QueryBuilder()
            dls.invoke(builder)
            query(builder.build())
        }
    }

    fun first(query: Query, predicate: (T) -> Boolean): T {
        return query(query).first(predicate)
    }

    fun firstAsync(query: Query, predicate: (T) -> Boolean): CompletableFuture<T> {
        return CompletableFuture.supplyAsync { query(query).first(predicate) }.exceptionally { ex -> throw ex }
    }

    fun first(query: QueryBuilder.() -> Unit, predicate: (T) -> Boolean): T {
        return this.query(query).first(predicate)
    }

    fun firstAsync(query: QueryBuilder.() -> Unit, predicate: (T) -> Boolean): CompletableFuture<T> {
        return CompletableFuture.supplyAsync { query(query).first(predicate) }.exceptionally { ex -> throw ex }
    }

    fun firstOrNull(query: Query, predicate: (T) -> Boolean): T? {
        return query(query).firstOrNull(predicate)
    }

    fun firstOrNullAsync(query: Query, predicate: (T) -> Boolean): CompletableFuture<T?> {
        return CompletableFuture.supplyAsync { query(query).firstOrNull(predicate) }.exceptionally { ex -> throw ex }
    }

    fun firstOrNull(query: QueryBuilder.() -> Unit, predicate: (T) -> Boolean): T {
        return this.query(query).first(predicate)
    }

    fun firstOrNullAsync(query: QueryBuilder.() -> Unit, predicate: (T) -> Boolean): CompletableFuture<T?> {
        return CompletableFuture.supplyAsync { query(query).firstOrNull(predicate) }.exceptionally { ex -> throw ex }
    }

    fun deleteAll() {
        realm.writeBlocking {
            val allObjects = this.query(kclass).find()
            delete(allObjects)
        }
    }

    fun deleteAllAsync(): CompletableFuture<Unit> {
        return CompletableFuture.supplyAsync(::deleteAll).exceptionally { ex -> throw ex }
    }

    fun count(): Long {
        return realm.query(kclass).count().find()
    }

    fun countAsync(): CompletableFuture<Long> {
        return CompletableFuture.supplyAsync(::count).exceptionally { ex -> throw ex }
    }

    fun isEmpty(): Boolean {
        return count() == 0L
    }

    fun isEmptyAsync(): CompletableFuture<Boolean> {
        return CompletableFuture.supplyAsync(::isEmpty).exceptionally { ex -> throw ex }
    }
}