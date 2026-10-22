plugins {
    id("kotlin-conventions")
}

dependencies {
    api(project(":server:shared-kernel"))
    implementation(project(":server:identity:api"))
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
}
