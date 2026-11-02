plugins {
    id("db-conventions")
    application
}

dependencies {
    implementation(project(":server:shared-kernel"))
    implementation(project(":server:identity:api"))
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.serialization.json)
    implementation(libs.bcrypt)
}
