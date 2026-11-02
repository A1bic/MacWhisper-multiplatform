plugins {
    id("db-conventions")
}

tasks.named("flywayMigrate") {
    dependsOn(":server:transcription:impl:flywayMigrate")
}
