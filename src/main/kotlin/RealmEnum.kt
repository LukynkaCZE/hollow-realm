package cz.lukynka.hollow

import io.realm.kotlin.types.RealmObject

class RealmEnum(name: String) : RealmObject {
    var enumDescription: String = name

    constructor(): this("empty")

    fun setVal(value: Enum<*>) {
        enumDescription = value.name
    }

    inline fun <reified D : Enum<D>> getVal(): D {
        return enumValueOf<D>(enumDescription)
    }

    companion object {
        fun <T : Enum<*>> of(enum: T): RealmEnum {
            return RealmEnum(enum.name)
        }
    }

    override fun toString(): String {
        return enumDescription
    }
}