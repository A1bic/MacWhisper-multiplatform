plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    `java-library`
}

group = "ru.kovalenko" + project.path.substringBeforeLast(':').replace(':', '.')

base.archivesName = project.path.removePrefix(":").replace(':', '-')

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
}

val libs = the<VersionCatalogsExtension>().named("libs")

dependencies {
    implementation(libs.findLibrary("kotlinx-serialization-json").get())
    testImplementation(platform(libs.findLibrary("junit-bom").get()))
    testImplementation(libs.findLibrary("junit-jupiter").get())
    testImplementation(libs.findLibrary("assertj").get())
    testRuntimeOnly(libs.findLibrary("junit-platform-launcher").get())
}

tasks.test {
    useJUnitPlatform()
}
