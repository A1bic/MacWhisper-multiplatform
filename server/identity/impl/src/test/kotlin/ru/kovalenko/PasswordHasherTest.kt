package ru.kovalenko

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PasswordHasherTest {
    private val hasher = PasswordHasher()

    @Test
    fun `hash does not contain the raw password`() {
        val hash = hasher.hash("password123")

        assertThat(hash).isNotEqualTo("password123").doesNotContain("password123")
    }

    @Test
    fun `verify accepts the original password`() {
        val hash = hasher.hash("password123")

        assertThat(hasher.verify("password123", hash)).isTrue()
    }

    @Test
    fun `verify rejects a different password`() {
        val hash = hasher.hash("password123")

        assertThat(hasher.verify("password124", hash)).isFalse()
    }

    @Test
    fun `same password gets a different hash each time`() {
        assertThat(hasher.hash("password123")).isNotEqualTo(hasher.hash("password123"))
    }
}
