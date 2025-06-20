package cz.lukynka.hollow

import cz.lukynka.hollow.Query.QueryBuilder
import io.realm.kotlin.Realm
import io.realm.kotlin.types.RealmObject
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KClass

abstract class RealmStorage<T : RealmObject>(val kclass: KClass<out RealmObject>) {

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
        val future = CompletableFuture<Unit>()
        CompletableFuture.runAsync {
            delete(value)
            future.complete(Unit)
        }
        return future
    }

    fun write(value: T) {
        realm.writeBlocking {
            this.copyToRealm(value)
        }
    }

    fun writeAsync(value: T): CompletableFuture<Unit> {
        val future = CompletableFuture<Unit>()

        CompletableFuture.runAsync {
            this.write(value)
            future.complete(Unit)
        }

        return future
    }

    fun compactAsync(): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()

        CompletableFuture.runAsync {
            future.complete(this.compact())
        }

        return future
    }

    fun query(query: Query): List<T> {
        val crit = realm.criteriaQuery(kclass, query)
        return realm.copyFromRealm(crit.find()) as List<T>
    }

    fun query(dls: QueryBuilder.() -> Unit): List<T> {
        val builder = QueryBuilder()
        dls.invoke(builder)
        val query = builder.build()

        return query(query)
    }

    fun queryAsync(query: Query): CompletableFuture<List<T>> {
        val future = CompletableFuture<List<T>>()
        CompletableFuture.runAsync {
            future.complete(query(query))
        }
        return future
    }

    fun queryAsync(dls: QueryBuilder.() -> Unit): CompletableFuture<List<T>> {
        val future = CompletableFuture<List<T>>()

        val builder = QueryBuilder()
        dls.invoke(builder)
        future.complete(query(builder.build()))

        return future
    }
}