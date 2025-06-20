package cz.lukynka.hollow

import io.realm.kotlin.Configuration
import io.realm.kotlin.Realm

object HollowRealm {

    lateinit var realm: Realm

    fun open(configuration: Configuration): Realm {
        realm = Realm.open(configuration)
        return realm
    }
}