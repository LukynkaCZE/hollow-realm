plugins {
    kotlin("jvm") version "2.0.20"
    id("io.realm.kotlin") version "3.0.0"
}

group = "cz.lukynka.hollow-realm"
version = "1.1"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    api("io.realm.kotlin:library-base:3.0.0")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}