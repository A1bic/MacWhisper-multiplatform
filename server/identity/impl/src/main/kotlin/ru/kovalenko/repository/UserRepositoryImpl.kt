package ru.kovalenko.repository

import org.jooq.DSLContext
import org.jooq.impl.DSL
import ru.kovalenko.UserId
import ru.kovalenko.identity.jooq.tables.references.USERS
import ru.kovalenko.repository.entity.NewUser
import ru.kovalenko.repository.entity.UserEntity
import ru.kovalenko.repository.mappers.toEntity
import kotlin.uuid.toJavaUuid

class UserRepositoryImpl(private val dsl: DSLContext) : UserRepository {
    override fun save(user: NewUser): UserEntity =
        dsl.insertInto(USERS)
//            .set(USERS.ID, user.id.value.toJavaUuid())
            .set(USERS.EMAIL, user.email)
            .set(USERS.PASSWORD_HASH, user.password)
            .set(USERS.DISPLAY_NAME, user.displayName)
            .returning()
            .fetchSingle()
            .toEntity()

    override fun findByEmail(email: String): UserEntity? =
        dsl.selectFrom(USERS)
            .where(DSL.lower(USERS.EMAIL).eq(email.lowercase()))
            .fetchOne()
            ?.toEntity()

    override fun delete(id: UserId) {
        dsl.deleteFrom(USERS)
            .where(USERS.ID.eq(id.value.toJavaUuid()))
            .execute()
    }
}