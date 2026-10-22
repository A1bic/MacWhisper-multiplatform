plugins {
    id("db-conventions")
}

dependencies {
    implementation(project(":server:transcription:api"))
    implementation(project(":server:recordings:api"))
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.json)
}

tasks.named("flywayMigrate") {
    dependsOn(":server:recordings:impl:flywayMigrate")
}
