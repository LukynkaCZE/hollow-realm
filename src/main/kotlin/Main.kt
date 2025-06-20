package cz.lukynka.hollow

import io.realm.kotlin.Realm
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class Player(
    @PrimaryKey
    var username: String,
    var level: Int,
    var gay: Boolean,
    var list: RealmList<String>,
) : RealmObject {
    constructor() : this("", 0, true, realmListOf())
}

fun main() {
    Realm.initialize {
        withSchema<Player>()
        deleteIfMigrationNeeded()
    }

    TestRealmStorage.write(Player("AsoDesu_", -1, true, realmListOf("test")))

    TestRealmStorage.queryAsync {
        equals("username", "LukynkaCZE")
        lessThanOrEquals("level", 3)
    }.thenAccept { result ->
        println("$result")
    }
}