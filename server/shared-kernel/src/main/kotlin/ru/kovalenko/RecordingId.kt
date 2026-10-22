package ru.kovalenko

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid


@JvmInline
value class RecordingId(val value: Uuid)