import org.yaml.snakeyaml.Yaml

plugins {
    id("kotlin-conventions")
    id("org.flywaydb.flyway")
    id("org.jooq.jooq-codegen-gradle")
}

val libs = the<VersionCatalogsExtension>().named("libs")
fun lib(alias: String) = libs.findLibrary(alias).get()

val appConfigFile = isolated.rootProject.projectDirectory.file("server/app/src/main/resources/application.yaml")
val appConfig: Map<String, Any?> = Yaml().load(providers.fileContents(appConfigFile).asText.get())

fun appSetting(path: String): String? {
    val raw = path.split('.')
        .fold<String, Any?>(appConfig) { node, key -> (node as? Map<*, *>)?.get(key) }
        ?.toString()
        ?: return null
    if (raw.startsWith("$$")) return raw.drop(1)
    if (!raw.startsWith("$")) return raw

    val keyWithDefault = raw.drop(1).removeSurrounding("{", "}")
    val name = keyWithDefault.substringBefore(':').removePrefix("?")
    val default = if (':' in keyWithDefault) keyWithDefault.substringAfter(':') else null
    return providers.systemProperty(name)
        .orElse(providers.environmentVariable(name))
        .orNull
        ?: default
}

val dbUrl = appSetting("db.url")
val dbUser = appSetting("db.user")
val dbPassword = appSetting("db.password")
val dbSchema = requireNotNull(project.parent?.name) { "db-conventions применяется к модулю вида :server:<имя>:impl" }

val migrationsDir = layout.projectDirectory.dir("src/main/resources/db/migration/$dbSchema")
val jooqDir = "src/main/jooq"

dependencies {
    implementation(lib("jooq"))
    implementation(lib("jooq-kotlin"))
    implementation(lib("flyway-core"))
    runtimeOnly(lib("flyway-database-postgres"))
    runtimeOnly(lib("postgres"))

    jooqCodegen(lib("postgres"))
}

flyway {
    url = dbUrl
    user = dbUser
    password = dbPassword
    schemas = arrayOf(dbSchema)
    locations = arrayOf("filesystem:${migrationsDir.asFile.absolutePath}")
}

jooq {
    configuration {
        jdbc {
            driver = "org.postgresql.Driver"
            url = dbUrl
            user = dbUser
            password = dbPassword
        }
        generator {
            name = "org.jooq.codegen.KotlinGenerator"
            strategy {
                matchers {
                    schemas {
                        schema {
                            schemaClass {
                                transform = org.jooq.meta.jaxb.MatcherTransformType.PASCAL
                                expression = "$0_schema"
                            }
                            schemaIdentifier {
                                transform = org.jooq.meta.jaxb.MatcherTransformType.UPPER
                                expression = "$0_schema"
                            }
                        }
                    }
                }
            }
            database {
                name = "org.jooq.meta.postgres.PostgresDatabase"
                inputSchema = dbSchema
                excludes = "flyway_schema_history"
            }
            generate {
                isKotlinNotNullRecordAttributes = true
            }
            target {
                packageName = "ru.kovalenko.$dbSchema.jooq"
                directory = jooqDir
            }
        }
    }
}

tasks.named("jooqCodegen") {
    dependsOn("flywayMigrate")
}

kotlin {
    sourceSets.main {
        kotlin.srcDir(jooqDir)
    }
}
