plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.kotlin.serialization.gradle.plugin)
    implementation(libs.flyway.gradle.plugin)
    implementation(libs.jooq.gradle.plugin)
    implementation(libs.jooq.meta)
    implementation(libs.snakeyaml)

    implementation(libs.flyway.database.postgres)
    implementation(libs.postgres)
}
