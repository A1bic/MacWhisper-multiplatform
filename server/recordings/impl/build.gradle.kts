plugins {
    id("db-conventions")
}

dependencies {
    implementation(project(":server:recordings:api"))
    implementation(libs.ktor.server.core)
    implementation(libs.minio)
}

tasks.named("flywayMigrate") {
    dependsOn(":server:identity:impl:flywayMigrate")
}
