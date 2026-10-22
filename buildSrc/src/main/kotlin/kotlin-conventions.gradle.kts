plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    `java-library`
}

group = "ru.kovalenko" + project.path.substringBeforeLast(':').replace(':', '.')

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
}

val libs = the<VersionCatalogsExtension>().named("libs")

dependencies {
    implementation(libs.findLibrary("kotlinx-serialization-json").get())
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
