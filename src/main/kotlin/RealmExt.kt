package cz.lukynka.hollow

import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.migration.RealmMigration
import io.realm.kotlin.types.RealmObject
import java.io.File
import kotlin.reflect.KClass

fun Realm.Companion.initialize(dsl: RealmConfigurationDsl.() -> Unit): Realm {
    val builder = RealmConfigurationDsl()
    dsl.invoke(builder)
    val configuration = builder.build()
    return HollowRealm.open(configuration)
}

class RealmConfigurationDsl {
    private val schemas: MutableSet<KClass<out RealmObject>> = mutableSetOf()
    private var deleteIfMigrationNeeded: Boolean = false
    private var compactOnLaunch: Boolean = false
    private var inMemory: Boolean = false
    private var schemaVersion: Long = 0
    private var migration: RealmMigration? = null
    private var directory: File? = null
    private var encryptionKey: ByteArray? = null
    private var name: String = "database.realm"

    fun withName(name: String) {
        this.name = name
    }

    fun withSchema(schema: KClass<out RealmObject>) {
        this.schemas.add(schema)
    }

    inline fun <reified T : RealmObject> withSchema() {
        withSchema(T::class)
    }

    fun deleteIfMigrationNeeded() {
        this.deleteIfMigrationNeeded = true
    }

    fun compactOnLaunch() {
        this.compactOnLaunch = true
    }

    fun inMemory() {
        this.inMemory = true
    }

    fun withSchemaVersion(schemaVersion: Long) {
        this.schemaVersion = schemaVersion
    }

    fun withSchemaVersion(schemaVersion: Int) {
        withSchemaVersion(schemaVersion.toLong())
    }

    fun withMigration(migration: RealmMigration) {
        this.migration = migration
    }

    fun withDirectory(directory: File) {
        if (!directory.isDirectory) throw IllegalArgumentException("$directory is not directory")
        this.directory = directory
    }

    fun withEncryption(encryptionKey: ByteArray) {
        this.encryptionKey = encryptionKey
    }

    fun build(): RealmConfiguration {
        val configuration = RealmConfiguration.Builder(
            schema = schemas
        )
        if (deleteIfMigrationNeeded) configuration.deleteRealmIfMigrationNeeded()
        if (compactOnLaunch) configuration.compactOnLaunch()
        if (inMemory) configuration.inMemory()
        configuration.schemaVersion(schemaVersion)
        if (migration != null) configuration.migration(migration!!)
        if (directory != null) configuration.directory(directory!!.absolutePath)
        if (encryptionKey != null) configuration.encryptionKey(encryptionKey!!)
        configuration.name(name)
        return configuration.build()
    }
}