package com.asaad27.life.util

import kotlin.jvm.JvmInline
import kotlin.math.PI

@JvmInline
value class Degrees(val value: Float)

@JvmInline
value class Radians(val value: Float)

fun Radians.toDegree() = Degrees(( this.value * 180f / PI ).toFloat())

fun Long.format(): String {
    return "todo"
}

fun Double.currencyFormat(): String {
    return "todo"
}

