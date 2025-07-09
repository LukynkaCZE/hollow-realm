# Hollow Realm

[![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https://mvn.devos.one/releases/cz/lukynka/hollow-realm/maven-metadata.xml&style=for-the-badge&logo=maven&logoColor=%23FFFFFF&label=Latest%20Version&color=%23afff87)](https://mvn.devos.one/#/releases/io/github/dockyardmc/dockyard)
[![Static Badge](https://img.shields.io/badge/Language-Kotlin-Kotlin?style=for-the-badge&color=%23963cf4)](https://kotlinlang.org/)

[![wakatime](https://wakatime.com/badge/user/7398c6f6-bec2-4b9c-b8b9-578d4a500952/project/f2380a4e-a08d-44a0-84f1-9111f5bbaff0.svg?style=for-the-badge)](https://wakatime.com/badge/github/DockyardMC/Scroll)
[![Discord](https://img.shields.io/discord/1242845647892123650?label=Discord%20Server&color=%237289DA&style=for-the-badge&logo=discord&logoColor=%23FFFFFF)](https://discord.gg/SA9nmfMkdc)
[![Static Badge](https://img.shields.io/badge/Donate-Ko--Fi-pink?style=for-the-badge&logo=ko-fi&logoColor=%23FFFFFF&color=%23ff70c8)](https://ko-fi.com/LukynkaCZE)

---

`hollow-realm` is utility and management library for [Realm](https://github.com/realm)

## Installation

<img src="https://cdn.worldvectorlogo.com/logos/kotlin-2.svg" width="16px"></img>
**Kotlin DSL**

```kotlin
repositories {
    maven("https://mvn.devos.one/releases")
}

dependencies {
    implementation("cz.lukynka:hollow-realm:<version>")
}
```

## Usage

First we want to write our classes. Note that due to realm limitations, some types like UUID, Lists and Maps use custom
objects like `RealmList`, `RealmUUID`, `RealmEnum` and every class needs to have empty constructor

```kotlin

class Player(
    @PrimaryKey
    var uuid: RealmUUID,
    var playerData: PlayerData?,
) : RealmObject {
    constructor() : this(RealmUUID.DEFAULT, PlayerData())
}

class PlayerData(
    var xp: Int,
    var level: Int,
    var rank: RealmEnum
) : EmbeddedRealmObject {
    constructor() : this(0, 0, RealmEnum.of(Rank.PLAYER))
}

enum class Rank {
    ADMIN,
    PLAYER
}
```

Then we want to initialize realm without classes:

```kotlin
Realm.initialize {
    withSchema<Player>()
    withSchema<PlayerData>()
    deleteIfMigrationNeeded()
}
```

Then we create our Realm Storage:

```kotlin
object PlayerStorage : RealmStorage<Player>(Player::class)
```

And finally we can operate on it:

```kotlin
val player = Player(RealmUUID.random(), PlayerData(6, 9, RealmEnum.of(Rank.PLAYER)))
PlayerStorage.write(player)

val maya = PlayerStorage.first { and { equals("name", "LukynkaCZE") } }
prinln(maya.name)

PlayerStorage.delete(maya)
```

All methods also have async variant with completable futures