package cz.lukynka.hollow

import io.realm.kotlin.Realm
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.RealmUUID
import io.realm.kotlin.types.annotations.PrimaryKey

class Player(
    @PrimaryKey
    var username: String,
    var level: Int,
    var gay: Boolean,
    var data: PlayerData?
) : RealmObject {
    constructor() : this("", 0, false, PlayerData())
}

class PlayerData(
    var xp: Int,
    var isBadAtTheGame: Boolean,
    var profile: GameProfile?,
) : RealmObject {
    constructor() : this(0, true, GameProfile())
}

class GameProfile(
    var name: String,
    var uuid: RealmUUID,
) : RealmObject {
    constructor() : this("", RealmUUID.random())
}

fun main() {
    Realm.initialize {
        withSchema<Player>()
        withSchema<PlayerData>()
        withSchema<GameProfile>()
        deleteIfMigrationNeeded()
    }

    TestRealmStorage.write(Player("LukynkaCZE", 1, false, PlayerData()))

    TestRealmStorage.allAsync().thenAccept { players ->
        println(players)
    }

    println("${TestRealmStorage.findById("LukynkaCZE")}")
}