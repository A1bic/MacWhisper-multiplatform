plugins {
    id("kotlin-conventions")
}

dependencies {
    implementation(project(":server:identity:impl"))
    implementation(project(":server:recordings:impl"))
    implementation(project(":server:summarization:impl"))
    implementation(project(":server:transcription:impl"))
    implementation(project(":server:identity:api"))
    implementation(project(":server:recordings:api"))
    implementation(project(":server:summarization:api"))
    implementation(project(":server:transcription:api"))

    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.serialization.json)
    runtimeOnly(libs.ktor.server.core)
    implementation(libs.minio)
    implementation(libs.slf4j.api)
    implementation(libs.ktor.server.config.yaml)
    implementation(libs.postgres)
    implementation(libs.hikari)
    implementation(libs.flyway.core)
    implementation(libs.flyway.database.postgres)
    implementation(libs.jooq)
    implementation(libs.jooq.kotlin)
}
