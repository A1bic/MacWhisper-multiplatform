plugins {
    id("kotlin-conventions")
}

dependencies {
    implementation(project(":server:shared-kernel"))
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.serialization.json)
}