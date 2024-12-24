package com.asaad27.life.util

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.jvm.JvmInline
import kotlin.math.PI

@JvmInline
value class Degrees(val value: Float)

@JvmInline
value class Radians(val value: Float)

fun Radians.toDegree() = Degrees(( this.value * 180f / PI ).toFloat())

fun Instant.toHhMm(tz: TimeZone = TimeZone.currentSystemDefault()): String {
    return this.toLocalDateTime(tz)
        .let { "${it.hour.toString().padStart(2, '0')}:${it.minute.toString().padStart(2, '0')}" }
}

fun Double.currencyFormat(): String {
    return "todo"
}