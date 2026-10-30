package ru.kovalenko

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import org.flywaydb.core.Flyway
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import ru.kovalenko.identity.jooq.IdentitySchema
import ru.kovalenko.recordings.jooq.RecordingsSchema
import ru.kovalenko.summarization.jooq.SummarizationSchema
import ru.kovalenko.transcription.jooq.TranscriptionSchema
import javax.sql.DataSource

fun Application.configureDatabase(): DSLContext {
    val ds = HikariDataSource(HikariConfig().apply {
        jdbcUrl = environment.config.property("db.url").getString()
        username = environment.config.property("db.user").getString()
        password = environment.config.property("db.password").getString()
    })
    monitor.subscribe(ApplicationStopped) {
        ds.close()
    }

    listOf(
        IdentitySchema.IDENTITY_SCHEMA,
        RecordingsSchema.RECORDINGS_SCHEMA,
        TranscriptionSchema.TRANSCRIPTION_SCHEMA,
        SummarizationSchema.SUMMARIZATION_SCHEMA,
    ).forEach { schema ->
        migrate(ds, schema.name)
    }

    return DSL.using(ds, SQLDialect.POSTGRES)
}

private fun migrate(dataSource: DataSource, schema: String) {
    Flyway.configure()
        .dataSource(dataSource)
        .schemas(schema)
        .locations("classpath:db/migration/$schema")
        .load()
        .migrate()
}
